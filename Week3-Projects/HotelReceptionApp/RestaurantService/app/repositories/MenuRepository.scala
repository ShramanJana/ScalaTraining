package repositories

import models.Menu
import play.api.db.slick.DatabaseConfigProvider
import slick.jdbc.JdbcProfile

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MenuRepository @Inject()(dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  val dbConfig = dbConfigProvider.get[JdbcProfile]

  import dbConfig._
  import profile.api._

  class MenuTable(tag: Tag) extends Table[Menu](tag, "menu") {
    def id = column[Int]("id", O.PrimaryKey)

    def foodItem = column[String]("food_item")

    def foodType = column[String]("food_type")

    def price = column[Double]("price")

    def * = (id, foodItem, foodType, price) <> ((Menu.apply _).tupled, Menu.unapply)
  }

  val menu = TableQuery[MenuTable]

  def list(): Future[Seq[Menu]] = db.run {
    menu.result
  }

  def insertMenuItem(insertList: Seq[Menu]): Future[Option[Int]] = db.run {
    menu.delete.andThen(menu ++= insertList)
  }
}
