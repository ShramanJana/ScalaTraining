package repositories

import models.User
import play.api.db.slick._
import slick.jdbc.JdbcProfile

import javax.inject._
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class UserRepository @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext)
  extends HasDatabaseConfigProvider[JdbcProfile] {

  import profile.api._

  class UserTable(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
    def username = column[String]("username")
    def role = column[String]("role")
    def email = column[String]("email")
    def createdBy = column[Int]("created_by")
    def * = (id, username, role, email, createdBy) <> ((User.apply _).tupled, User.unapply)
  }

  private val users = TableQuery[UserTable]

  def findById(userId: Int): Future[Option[User]] = {
    db.run(users.filter(_.id === userId).result.headOption)
  }

  def addNewUser(user: User): Future[Int] = db.run {
    users.returning(users.map(_.id)) += user
  }

  def updateUser(id: Int, user: User): Future[Int] = db.run {
    users.filter(_.id === id).update(user)
  }
}
