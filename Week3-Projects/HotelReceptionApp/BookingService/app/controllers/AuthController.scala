package controllers

import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.{JsError, JsValue, Json, OFormat}
import play.api.mvc.{AbstractController, Action, ControllerComponents}
import repositories.{Admin, AdminRepository}
import security.JwtHelper

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthController @Inject()(cc: ControllerComponents, adminRepository: AdminRepository)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  implicit val userFormat: OFormat[Admin] = Json.format[Admin]

  def login: Action[JsValue] = Action.async(parse.json) { request =>
    val username = (request.body \ "username").as[String]
    val password = (request.body \ "password").as[String]

    adminRepository.findByUsername(username).map {
      case Some(user) if BCrypt.checkpw(password, user.password) =>
        val token = JwtHelper.generateToken(username)
        Ok(Json.obj("message" -> "Login successful", "token" -> token))
      case _ =>
        Unauthorized(Json.obj("status" -> "error", "message" -> "Invalid credentials"))
    }
  }

  def register: Action[JsValue] = Action.async(parse.json) { request =>
    request.body.validate[Admin].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> "Invalid user data format", "errorMessage" -> JsError.toJson(errors))))
      },
      newUser => {
        val username = newUser.username
        val password = newUser.password
        val passwordHash = BCrypt.hashpw(password, BCrypt.gensalt)

        adminRepository.usernameExists(username).flatMap {
          case true => Future.successful(Conflict(Json.obj("status" -> "error", "message" -> "User already exists")))
          case false =>
            adminRepository.create(Admin(None, username, passwordHash)).map { _ =>
              Created(Json.obj("status" -> "success", "message" -> "User created successfully"))
            }
        }
      } recover {
        case ex: Exception => Conflict(Json.obj("status" -> "error", "message" -> ex.getMessage))
      }
    )
  }
}
