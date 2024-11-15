package actors

import akka.actor.{Actor, Props}
import models.Reservation
import services.EmailService

object ReminderActor {
  def props(emailService: EmailService): Props = Props(new ReminderActor(emailService))
}

class ReminderActor(emailService: EmailService) extends Actor {
  override def receive: Receive = {
    case reservation: Reservation =>
      context.system.log.info(s"Sending reminder for reservation ID: ${reservation.id}")
      emailService.sendReminder(reservation)
  }
}
