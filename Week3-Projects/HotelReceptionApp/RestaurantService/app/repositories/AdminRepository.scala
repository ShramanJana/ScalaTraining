package repositories

import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile
import slick.lifted.ProvenShape

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class Admin(id: Option[Long], username: String, password: String)

class AdminRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  private val dbConfig = dbConfigProvider.get[JdbcProfile]
  import dbConfig._
  import profile.api._

  private class AdminTable(tag: Tag) extends Table[Admin](tag, "admin") {
    def id: Rep[Long] = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def username: Rep[String] = column[String]("username", O.Unique)
    def password: Rep[String] = column[String]("password")

    override def * : ProvenShape[Admin] = (id.?, username, password) <> (Admin.tupled, Admin.unapply)
  }

  private val adminTable = TableQuery[AdminTable]

  def findByUsername(username: String): Future[Option[Admin]] =
    db.run(adminTable.filter(_.username === username).result.headOption)

  def usernameExists(username: String): Future[Boolean] = {
    db.run(adminTable.filter(_.username === username).exists.result)
  }

  def create(admin: Admin): Future[Admin] =
    db.run((adminTable returning adminTable.map(_.id) into ((user, id) => user.copy(id = Some(id)))) += admin)
}
