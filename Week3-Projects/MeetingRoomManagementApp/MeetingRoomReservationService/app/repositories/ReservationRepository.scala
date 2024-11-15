package repositories

import models.{Reservation, Room}
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReservationRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  // Define the ReservationTable for the Reservation model
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

  // Define the RoomTable for the Room model
  class RoomTable(tag: Tag) extends Table[Room](tag, "rooms") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def roomName = column[String]("room_name")
    def capacity = column[Int]("capacity")
    def location = column[String]("location")

    def * = (id, roomName, capacity, location) <> ((Room.apply _).tupled, Room.unapply)
  }

  val rooms = TableQuery[RoomTable]

  // Find available rooms for a given time range with room details
  def findAvailableRooms(startTime: String, endTime: String): Future[List[Room]] = {
    // First, find rooms that are reserved during the given time range
    val reservedRoomIdsQuery = reservations
      .filter(reservation =>
        reservation.startTime < endTime && reservation.endTime > startTime
      )
      .map(_.roomId)
      .distinct

    // Then, select rooms that are not in the reservedRoomIds list
    val availableRoomsQuery = rooms.filterNot(_.id in reservedRoomIdsQuery)

    db.run(availableRoomsQuery.result).map(_.toList)
  }

  // Method to create a reservation
  def createReservation(reservation: Reservation): Future[Int] = {
    val insertQuery = reservations.returning(reservations.map(_.id)) += reservation
    db.run(insertQuery).map(_.getOrElse(throw new Exception("Failed to create reservation")))
  }

  def checkRoomAvailability(roomId: Int, startTime: String, endTime: String): Future[Boolean] = {
    val conflictingReservations = reservations
      .filter(reservation =>
        reservation.roomId === roomId &&
          reservation.startTime < endTime &&
          reservation.endTime > startTime
      )
    db.run(conflictingReservations.exists.result).map(!_)
  }


}
