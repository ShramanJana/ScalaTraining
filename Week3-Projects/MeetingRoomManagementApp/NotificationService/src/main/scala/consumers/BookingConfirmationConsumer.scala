package consumers

import actors.{BookingConfirmationActor, ReminderActor}
import akka.actor.{ActorSystem, Props}
import io.circe.parser.decode
import models.Reservation
import utils.JsonFormats.*
import utils.KafkaConsumerUtil.createConsumer

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.Collections
import scala.concurrent.duration.*

object BookingConfirmationConsumer {
  def startConsumer()(implicit system: ActorSystem): Unit = {
    val consumer = createConsumer("bookingConfirmationGroup")
    consumer.subscribe(Collections.singletonList("meeting_reservation"))
    
    // Create ReminderActor
    val reminderActor = system.actorOf(Props(new ReminderActor()), "reminderActor")
    // Create Booking Confirmation Actor
    val bookingConfirmationActor = system.actorOf(Props(new BookingConfirmationActor()), "bookingConfirmationActor")

    system.log.info("Booking Confirmation Consumer started")

    while (true) {
      val records = consumer.poll(java.time.Duration.ofMillis(100))
      records.forEach { record =>
        system.log.info(s"Received message: ${record.value()}")
        decode[Reservation](record.value()) match {
          case Right(reservation) =>
            system.log.info(s"Decoded reservation: $reservation") 
            bookingConfirmationActor ! reservation

            // Send reminder mail to user 15 mins before the meeting
            val reminderTime = LocalDateTime.parse(reservation.startTime).minusMinutes(15)
            val reminderDelay = ChronoUnit.MILLIS.between(LocalDateTime.now(), reminderTime)

            if (reminderDelay > 0) {
              system.scheduler.scheduleOnce(
                reminderDelay.milliseconds,
                reminderActor,
                reservation
              )(system.dispatcher)
              system.log.info(s"Scheduled reminder for reservation ID: ${reservation.id} at $reminderTime")
            } else {
              system.log.warning(s"Skipping reminder for reservation ID: ${reservation.id} as it's too close or past start time")
            }

          case Left(error) =>
            system.log.error(s"Failed to decode JSON to Reservation: ${error.getMessage}")
        }
      }
    }
  }
}
