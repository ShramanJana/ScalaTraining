package actors

import akka.actor.{Actor, Props}
import models.Reservation
import services.EmailService
import services.EmailService.sendRoomPreparationNotification

class RoomPreparationActor extends Actor {
  def receive: Receive = {
    case reservation: Reservation =>
      sendRoomPreparationNotification(reservation)
  }
}
