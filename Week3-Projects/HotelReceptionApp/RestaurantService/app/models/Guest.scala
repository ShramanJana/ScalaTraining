package models

import play.api.libs.json.{Json, Reads}

case class Guest(guestId: Long, name: String, roomNo: Int, email: String, address: String, guestStatus: String)

object Guest {
  // Define the Reads for Person to allow Play JSON to map JSON to the case class
  implicit val guestReads: Reads[Guest] = Json.reads[Guest]
}
