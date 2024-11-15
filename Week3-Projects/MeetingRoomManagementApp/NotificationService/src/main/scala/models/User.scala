package models

case class User(
                 id: Int,
                 username: String,
                 role: String, // e.g., "AdminStaff", "RoomService", "SystemAdmin"
                 email: String
               )
