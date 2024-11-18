package actors

import akka.actor.{Actor, Props}
import models.Reservation
import services.EmailService
import services.EmailService.sendBookingConfirmation

class BookingConfirmationActor extends Actor {
  def receive: Receive = {
    case reservation: Reservation =>
      sendBookingConfirmation(reservation)
  }
}