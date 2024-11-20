package consumers

import actors.{ReleaseActor, RoomPreparationActor}
import akka.actor.{ActorSystem, Props}
import io.circe.parser.decode
import models.Reservation
import services.RoomService
import utils.JsonFormats.*
import utils.KafkaConsumerUtil.createConsumer

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Collections
import scala.concurrent.duration.*

object RoomPreparationConsumer {
  def startConsumer()(implicit system: ActorSystem): Unit = {
    val consumer = createConsumer("roomPreparationGroup")
    consumer.subscribe(Collections.singletonList("reservation-created"))
    // Create Room Preparation Actor
    val roomPreparationActor = system.actorOf(Props(new RoomPreparationActor()), "roomPreparationActor")
    // Instantiate the RoomService
    val roomService = new RoomService()
    // Create ReleaseActor
    val releaseActor = system.actorOf(Props(new ReleaseActor(roomService)), "releaseActor")

    system.log.info("Room Preparation Consumer started")
    while (true) {
      val records = consumer.poll(java.time.Duration.ofMillis(100))
      records.forEach { record =>
        decode[Reservation](record.value()) match {
          case Right(reservation) =>
            // Temporary testing: Reminder 10 seconds before start time
            val preparationTime = LocalDateTime.parse(reservation.startTime).minusMinutes(15)
            val preparationDelay = ChronoUnit.MILLIS.between(LocalDateTime.now(), preparationTime)

            if (preparationDelay > 0) {
              system.scheduler.scheduleOnce(
                preparationDelay.milliseconds,
                roomPreparationActor,
                reservation
              )(system.dispatcher)
              system.log.info(s"Scheduled reminder for reservation ID: ${reservation.id} at $preparationTime")
            } else {
              system.log.warning(s"Skipping reminder for reservation ID: ${reservation.id} as it's too close or past start time")
            }

            val releaseTime = LocalDateTime.parse(reservation.endTime).plusMinutes(15)
            val releaseDelay = ChronoUnit.MILLIS.between(LocalDateTime.now(), releaseTime)

            if (releaseDelay > 0) {
              system.scheduler.scheduleOnce(
                releaseDelay.milliseconds,
                releaseActor,
                reservation
              )(system.dispatcher)
              system.log.info(s"Scheduled release check for reservation ID: ${reservation.id} at $releaseTime")
            } else {
              system.log.warning(s"Skipping release check for reservation ID: ${reservation.id} as it's too close or past start time")

            }
          case Left(error) =>
            system.log.error(s"Failed to decode JSON to Reservation: ${error.getMessage}")
        }
      }
    }
  }
}
