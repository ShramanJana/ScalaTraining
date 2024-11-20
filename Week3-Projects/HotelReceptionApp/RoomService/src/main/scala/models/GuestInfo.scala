package models

import spray.json.DefaultJsonProtocol._
import spray.json._

object JsonFormats {
  implicit val guestFormat: RootJsonFormat[GuestInfo] = jsonFormat2(GuestInfo)
}

case class GuestInfo(name: String, email: String)
