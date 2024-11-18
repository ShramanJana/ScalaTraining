package controllers

import models.{Room, User}
import play.api.libs.json.{JsError, JsValue, Json, OFormat}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import services.{RoomService, UserService}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdminController @Inject()(cc: ControllerComponents, roomService: RoomService, userService: UserService)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val reservationFormat: OFormat[User] = Json.format[User]
  implicit val roomFormat: OFormat[Room] = Json.format[Room]

  def createNewUser: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[User].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> "Invalid user data format", "errorMessage" -> JsError.toJson(errors))))
      },
      newUser => {
        userService.getUserById(newUser.createdBy).flatMap {
          case Some(user) if user.role == "SystemAdmin" => userService.addNewUser(newUser).map(_ => Created(Json.toJson(newUser)))
          case Some(_) => Future.successful(Forbidden(Json.obj("error" -> "Only System Admin can create users")))
          case None => Future.successful(NotFound(Json.obj("error" -> "User not found")))
        }
      }
    )
  }

  def addNewRoom: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Room].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> "Invalid room data format", "errorMessage" -> JsError.toJson(errors))))
      },
      room => {
        userService.getUserById(room.createdBy).flatMap {
          case Some(user) if user.role == "SystemAdmin" => roomService.addNewRoom(room).map(_ => Created(Json.toJson(room)))
          case Some(_) => Future.successful(Forbidden(Json.obj("error" -> "Only System Admin can create meeting rooms")))
          case None => Future.successful(NotFound(Json.obj("error" -> "User not found")))
        }
      }
    )
  }

  def updateRoom(id: Int): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Room].fold(
      errors => Future.successful(BadRequest("Invalid JSON provided")),
      room => {
        userService.getUserById(room.createdBy).flatMap {
          case Some(user) if user.role == "SystemAdmin" => roomService.updateRoom(id, room).map(_ => Ok(Json.toJson(room)))
          case Some(_) => Future.successful(Forbidden(Json.obj("error" -> "Only System Admin can update meeting rooms")))
          case None => Future.successful(NotFound(Json.obj("error" -> "User not found")))
        }
      }
    )
  }

  def updateUser(id: Int): Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[User].fold(
      errors => Future.successful(BadRequest("Invalid JSON provided")),
      requestUser => {
        userService.getUserById(requestUser.createdBy).flatMap {
          case Some(user) if user.role == "SystemAdmin" => userService.updateUser(id, requestUser).map(_ => Ok(Json.toJson(requestUser)))
          case Some(_) => Future.successful(Forbidden(Json.obj("error" -> "Only System Admin can update users")))
          case None => Future.successful(NotFound(Json.obj("error" -> "User not found")))
        }
      }
    )
  }

}
