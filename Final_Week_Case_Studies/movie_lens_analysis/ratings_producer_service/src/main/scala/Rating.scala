import spray.json.{DefaultJsonProtocol, RootJsonFormat}
import spray.json.DefaultJsonProtocol.jsonFormat4

// Case class to represent a movie rating
case class Rating(userId: Int, movieId: Int, rating: Double, timestamp: Long)

// JSON Protocol for Movie Rating case class
object RatingJsonProtocol extends DefaultJsonProtocol {
  implicit val ratingFormat: RootJsonFormat[Rating] = jsonFormat4(Rating)
}
