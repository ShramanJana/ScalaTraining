package services

import models.Reservation

class EmailService {
  def sendBookingConfirmation(reservation: Reservation): Unit = {
    println(s"Sending booking confirmation to ${reservation.employeeName} for reservation ID ${reservation.id}")
  }

  def sendRoomPreparationNotification(reservation: Reservation): Unit = {
    println(s"Sending room preparation notification for reservation ID ${reservation.id}")
  }

  def sendReminder(reservation: Reservation): Unit = {
    println(s"Sending reminder to ${reservation.employeeName} for reservation ID ${reservation.id}")
  }

  def sendReleaseNotification(reservation: Reservation): Unit = {
    println(s"Room ${reservation.roomId} was not used for reservation ID ${reservation.id}. Notification sent to admin staff.")
  }
}
