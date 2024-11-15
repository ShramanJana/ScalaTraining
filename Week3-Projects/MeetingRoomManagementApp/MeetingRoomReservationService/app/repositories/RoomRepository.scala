package repositories

import models.{Reservation, Room}
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RoomRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Define RoomTable for the Room model
  class RoomTable(tag: Tag) extends Table[Room](tag, "rooms") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def roomName = column[String]("room_name")
    def capacity = column[Int]("capacity")
    def location = column[String]("location")

    def * = (id, roomName, capacity, location) <> ((Room.apply _).tupled, Room.unapply)
  }

  val rooms = TableQuery[RoomTable]

  // Define ReservationTable for the Reservation model
  class ReservationTable(tag: Tag) extends Table[Reservation](tag, "reservations") {
    def id = column[Option[Int]]("id", O.PrimaryKey, O.AutoInc)
    def roomId = column[Int]("room_id")
    def employeeName = column[String]("employee_name")
    def department = column[String]("department")
    def purpose = column[String]("purpose")
    def startTime = column[String]("start_time")
    def endTime = column[String]("end_time")
    def createdBy = column[Int]("created_by")

    def * = (id, roomId, employeeName, department, purpose, startTime, endTime, createdBy) <> ((Reservation.apply _).tupled, Reservation.unapply)
  }

  val reservations = TableQuery[ReservationTable]

  // Find available rooms based on the specified time range
  def findAvailableRooms(startTime: String, endTime: String): Future[List[Room]] = {
    // Query for rooms that are not booked during the specified time range
    val availableRoomsQuery = rooms.filterNot { room =>
      reservations
        .filter(reservation => reservation.roomId === room.id)
        .filter(reservation => reservation.startTime < endTime && reservation.endTime > startTime)
        .exists
    }

    db.run(availableRoomsQuery.result).map(_.toList)
  }

  def findById(roomId: Int): Future[Option[Room]] = {
    db.run(rooms.filter(_.id === roomId).result.headOption)
  }

}
