package controllers

import models.{Reservation, Room, User}
import play.api.libs.json._
import play.api.mvc._
import services.{ReservationService, RoomService, UserService}
import utils.KafkaProducerUtil

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

@Singleton
class ReservationController @Inject()(cc: ControllerComponents, reservationService: ReservationService, roomService: RoomService, userService: UserService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  // Implicit JSON format for Reservation and Room models
  implicit val reservationFormat: OFormat[Reservation] = Json.format[Reservation]
  implicit val roomFormat: OFormat[Room] = Json.format[Room]
  implicit val userFormat: OFormat[User] = Json.format[User]

  // Endpoint to reserve a room, restricted to AdminStaff role
  def reserveRoom: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Reservation].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> "Invalid reservation data format", "errorMessage" -> JsError.toJson(errors))))
      },
      reservation => {
        // Check if the user has the AdminStaff role
        userService.getUserById(reservation.createdBy).flatMap {
          case Some(user) if user.role == "AdminStaff" =>
            reservationService.reserveRoom(reservation).map {
              case Some(savedReservation) =>
                // Trigger Kafka event after successful reservation creation
                val reservationData = Json.toJson(savedReservation).as[JsObject]
                Await.result(userService.getUserById(reservation.employeeId), Duration.apply(3, TimeUnit.SECONDS)).foreach {
                  employee: User =>
                    val userData = Json.toJson(employee).as[JsObject]
                    // merge user data and reservation data
                    val kafkaMessage = reservationData.deepMerge(userData - "id").toString()
                    KafkaProducerUtil.sendMessage("meeting_reservation", savedReservation.id.toString, kafkaMessage)
                }

                Created(Json.toJson(savedReservation))
              case None =>
                Conflict(Json.obj("error" -> "Room is unavailable for the selected time"))
            }
          case Some(_) =>
            Future.successful(Forbidden(Json.obj("error" -> "Only Admin Staff can make reservations")))
          case None =>
            Future.successful(NotFound(Json.obj("error" -> "User not found")))
        }
      }
    )
  }

  // Endpoint to check available rooms for a given time range
  def checkAvailableRooms(startTime: String, endTime: String): Action[AnyContent] = Action.async {
    roomService.findAvailableRooms(startTime, endTime).map { availableRooms =>
      Ok(Json.toJson(availableRooms))
    }
  }

  // Endpoint to check availability of a specific room
  def checkRoomAvailability(roomId: Int, startTime: String, endTime: String): Action[AnyContent] = Action.async {
    roomService.getRoomById(roomId).flatMap {
      case Some(_) => // Room exists, proceed to check availability
        reservationService.checkRoomAvailability(roomId, startTime, endTime).map { isAvailable =>
          if (isAvailable) {
            Ok(Json.obj("available" -> true))
          } else {
            Conflict(Json.obj("available" -> false, "message" -> "Room is already reserved for this time range."))
          }
        }
      case None => // Room not found
        Future.successful(NotFound(Json.obj("error" -> "Room not found")))
    }
  }
}
