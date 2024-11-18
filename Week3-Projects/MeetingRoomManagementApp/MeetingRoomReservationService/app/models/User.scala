package models

import play.api.libs.json.{Json, OFormat}

case class User(
                 id: Int,
                 username: String,
                 role: String, // Role can be 'AdminStaff', 'RoomService', or 'SystemAdmin'
                 email: String,
                 createdBy: Int
               )

object User {
  implicit val userFormat: OFormat[User] = Json.format[User]
}
