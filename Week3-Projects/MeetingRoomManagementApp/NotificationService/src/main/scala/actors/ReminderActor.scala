package actors

import akka.actor.{Actor, Props}
import models.Reservation
import services.EmailService
import services.EmailService.sendReminder

class ReminderActor() extends Actor {
  override def receive: Receive = {
    case reservation: Reservation =>
      context.system.log.info(s"Sending reminder for reservation ID: ${reservation.id}")
      sendReminder(reservation)
  }
}
