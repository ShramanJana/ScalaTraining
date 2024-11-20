package services

import models.Room
import repositories.RoomRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RoomService @Inject()(roomRepository: RoomRepository)(implicit ec: ExecutionContext) {

  def getRoomById(roomId: Int): Future[Option[Room]] = {
    roomRepository.findById(roomId)
  }

  def findAvailableRooms(startTime: String, endTime: String): Future[List[Room]] = {
    roomRepository.findAvailableRooms(startTime, endTime).map(_.toList)
  }

  def addNewRoom(room: Room): Future[Int] = {
    roomRepository.addNewRoom(room)
  }

  def updateRoom(id: Int, room: Room): Future[Int] = roomRepository.updateRoom(id, room)

}
