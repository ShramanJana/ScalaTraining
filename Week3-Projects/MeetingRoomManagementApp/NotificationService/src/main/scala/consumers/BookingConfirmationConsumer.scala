package consumers

import actors.{ReleaseActor, ReminderActor}
import akka.actor.ActorSystem
import io.circe.parser.decode
import models.Reservation
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.serialization.StringDeserializer
import services.{EmailService, RoomService}
import utils.JsonFormats.*

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.{Collections, Properties}
import scala.concurrent.duration.*

object BookingConfirmationConsumer {
  def startConsumer()(implicit system: ActorSystem): Unit = {
    val props = new Properties()
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "booking-confirmation-group")
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, classOf[StringDeserializer].getName)

    val consumer = new KafkaConsumer[String, String](props)
    consumer.subscribe(Collections.singletonList("reservation-created"))

    val emailService = new EmailService()  // Instantiate the EmailService
    val roomService = new RoomService()    // Instantiate the RoomService
    val reminderActor = system.actorOf(ReminderActor.props(emailService), "reminderActor") // Create ReminderActor
    val releaseActor = system.actorOf(ReleaseActor.props(emailService, roomService), "releaseActor") // Create ReleaseActor

    system.log.info("Booking Confirmation Consumer started")

    while (true) {
      val records = consumer.poll(java.time.Duration.ofMillis(100))
      records.forEach { record =>
        system.log.info(s"Received message: ${record.value()}") // Log received messages
        decode[Reservation](record.value()) match {
          case Right(reservation) =>
            system.log.info(s"Decoded reservation: $reservation") // Log successful decoding
            system.actorSelection("/user/bookingConfirmationActor") ! reservation

            // Temporary testing: Reminder 10 seconds before start time
            val reminderTime = LocalDateTime.parse(reservation.startTime).minusSeconds(10)
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

            // Temporary testing: Release check 10 seconds after the start time
            val releaseTime = LocalDateTime.parse(reservation.startTime).plusSeconds(10)
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