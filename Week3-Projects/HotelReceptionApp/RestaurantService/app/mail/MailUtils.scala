package mail

import models.{Email, Guest, GuestInfo, Menu}

import java.util.Properties
import javax.mail.internet.{InternetAddress, MimeMessage}
import javax.mail.{Authenticator, Message, MessagingException, PasswordAuthentication, Session, Transport}

object MailUtils {
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

  def composeMail(guest: GuestInfo, menuList: Seq[Menu]): Email = {
    val listItems = menuList.map { menu =>
      s"<li>Item Name: ${menu.foodItem}, Category: ${menu.foodType}, Price: ${menu.price}</li>"
    }.mkString("\n") // Join all list items with new lines

    // Wrap the list items in a <ul> tag to create the full HTML content
    val content: String = s"""
       |<html>
       |<head>
       |  <title>Today's Menu</title>
       |  <style>
       |    body {
       |      font-family: Arial, sans-serif;
       |    }
       |    ul {
       |      list-style-type: none;
       |      padding: 0;
       |    }
       |    li {
       |      background-color: #f4f4f4;
       |      margin: 5px 0;
       |      padding: 10px;
       |      border-radius: 4px;
       |    }
       |  </style>
       |</head>
       |<body>
       |  <h2>Very good morning ${guest.name}</h2>
       |
       |  <h2>Please check today's menu at out in-house restaurant</h2>
       |  <ul>
       |    $listItems
       |  </ul>
       |  <p>Regards</p>
       |  <p>Team Originals</p>
       |</body>
       |</html>
    """.stripMargin
    Email(guest.email, "Today's Menu", content)
  }

  def composeAndSendEmail(guestInfo :GuestInfo, menu: Seq[Menu]): Unit = {
    val mailContent = composeMail(guestInfo, menu)
    sendEmail(mailContent)
  }

  def composeAndSendEmailAllGuests(guestList: Seq[Guest], menu: Seq[Menu]): Unit = {
    guestList.foreach(guest => composeAndSendEmail(GuestInfo(guest.name, guest.email), menu))
  }

  private def sendEmail(email: Email): Unit = {

    try {
      val message = new MimeMessage(session)
      message.setFrom(new InternetAddress(senderMail))
      message.setRecipients(Message.RecipientType.TO, email.receiverId)
      message.setSubject(email.subject)
      message.setContent(email.body, "text/html; charset=utf-8")
      Transport.send(message)
      println(s"Email sent to ${email.receiverId}")
    } catch {
      case e: MessagingException =>
        e.printStackTrace()
    }
  }

}
