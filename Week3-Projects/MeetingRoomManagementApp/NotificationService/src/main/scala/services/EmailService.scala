package services

import models.{Email, Reservation}
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail._
import java.util.Properties

object EmailService {
  val properties: Properties = new Properties()
  properties.put("mail.smtp.host", "smtp.gmail.com") // Replace with your SMTP server
  properties.put("mail.smtp.port", "587")
  properties.put("mail.smtp.auth", "true")
  properties.put("mail.smtp.starttls.enable", "true")

  val senderMail: String = sys.env.getOrElse("SENDER_MAIL", "shramanjana2015@gmail.com")
  val password: String = sys.env.getOrElse("SENDER_MAIL_PASSWORD", "")
  private val session = Session.getInstance(properties, new Authenticator() {
    override protected def getPasswordAuthentication =
      new PasswordAuthentication(senderMail, password)
  })
  private def composeConfirmationMail(reservation: Reservation): Email = {
    val body: String = s"Hi ${reservation.username},\n\nMeeting Confirmed!!\n\nyour ${reservation.purpose} meeting room booking has been confirmed"
    Email(reservation.email, "Meeting Reminder", body)
  }

  private def composeReminderMail(reservation: Reservation): Email = {
    val body: String = s"Hi ${reservation.username},\n\nGentle Reminder!!\n\nPlease join the ${reservation.purpose} meeting in 15 mins"
    Email(reservation.email, "Meeting Reminder", body)
  }

  private def sendEmail(email: Email): Unit = {
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(senderMail))
      message.setRecipients(Message.RecipientType.TO, email.receiverId)
      message.setSubject(email.subject)
      message.setText(email.body)

      Transport.send(message)
      println(s"Email sent to ${email.receiverId}")
    } catch {
      case e: MessagingException =>
        e.printStackTrace()
    }
  }
  def sendBookingConfirmation(reservation: Reservation): Unit = {
    println(s"Sending booking confirmation to ${reservation.username} for reservation ID ${reservation.id}")
    val email = composeConfirmationMail(reservation)
    sendEmail(email)
  }

  def sendRoomPreparationNotification(reservation: Reservation): Unit = {
    println(s"Sending room preparation notification for reservation ID ${reservation.id}")
  }

  def sendReminder(reservation: Reservation): Unit = {
    println(s"Sending reminder to ${reservation.username} for reservation ID ${reservation.id}")
    val email = composeConfirmationMail(reservation)
    sendEmail(email)
  }

  def sendReleaseNotification(reservation: Reservation): Unit = {
    println(s"Room ${reservation.roomId} was not used for reservation ID ${reservation.id}. Notification sent to admin staff.")
  }
}
