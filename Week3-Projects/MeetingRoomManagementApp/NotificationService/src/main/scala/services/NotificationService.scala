package services

import models.Reservation

class NotificationService {

  def sendBookingConfirmation(reservation: Reservation): Unit = {
    sendBookingConfirmation(reservation)
  }

  def sendRoomPreparationNotification(reservation: Reservation): Unit = {
    sendRoomPreparationNotification(reservation)
  }
}
