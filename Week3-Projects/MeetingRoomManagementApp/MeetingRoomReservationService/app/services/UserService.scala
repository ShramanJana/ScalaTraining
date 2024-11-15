package services

import models.User
import repositories.UserRepository

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserService @Inject()(userRepository: UserRepository)(implicit ec: ExecutionContext) {

  def getUserById(userId: Int): Future[Option[User]] = {
    userRepository.findById(userId)
  }
}
