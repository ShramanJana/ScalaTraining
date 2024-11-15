package models

import play.api.libs.json._

case class Room(id: Int, roomName: String, capacity: Int, location: String)

object Room {
  implicit val roomFormat: OFormat[Room] = Json.format[Room]
}
