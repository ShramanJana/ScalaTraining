package actors

import actors.MailSenderActorSystem.session
import akka.actor.{Actor, ActorSystem, Props}
import models.Email

import java.util.Properties
import javax.mail.{Authenticator, Message, MessagingException, PasswordAuthentication, Session, Transport}
import javax.mail.internet.{InternetAddress, MimeMessage}

class WifiMailSender extends Actor {
  def receive: Receive = {
    case email: Email => sendEmail(email)
  }
  private def sendEmail(email: Email): Unit = {
    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress("shramanjana2015@gmail.com"))
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
}


object MailSenderActorSystem {
  val properties: Properties = new Properties()
  properties.put("mail.smtp.host", "smtp.gmail.com") // Replace with your SMTP server
  properties.put("mail.smtp.port", "587")
  properties.put("mail.smtp.auth", "true")
  properties.put("mail.smtp.starttls.enable", "true")
  val senderMail: String = sys.env.getOrElse("SENDER_MAIL", "shramanjana2015@gmail.com")
  val password: String = sys.env.getOrElse("SENDER_MAIL_PASSWORD", "")
  val session = Session.getInstance(properties, new Authenticator() {
    override protected def getPasswordAuthentication =
      new PasswordAuthentication(senderMail, password)
  })
  val system = ActorSystem("WifiMailSenderSystem")
  val wifiMailSender = system.actorOf(Props[WifiMailSender], "WifiMailSender")
}
