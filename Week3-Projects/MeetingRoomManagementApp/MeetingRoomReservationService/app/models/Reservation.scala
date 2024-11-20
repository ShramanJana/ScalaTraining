package models

import play.api.libs.json.{Json, OFormat}

case class Reservation(id: Int,
                       roomId: Int,
                       employeeId: Int,
                       purpose: String,
                       startTime: String,
                       endTime: String,
                       createdBy: Int)

object Reservation {
  // Implicit JSON format for Reservation (used for both reading and writing JSON)
  implicit val reservationFormat: OFormat[Reservation] = Json.format[Reservation]
}
