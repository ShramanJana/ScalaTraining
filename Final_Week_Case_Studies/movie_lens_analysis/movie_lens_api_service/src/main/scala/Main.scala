import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpHeader}
import akka.http.scaladsl.model.StatusCodes.OK
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions.desc
import spray.json._

import javax.ws.rs._
import scala.concurrent.ExecutionContextExecutor

// Define case class and JSON format for serialization
case class MovieMetrics(movieId: Int, title: String, genres: String, averageRating: Double, totalRating: Long)
case class GenreMetrics(genre: String, averageRating: Double, totalRating: Long)
case class DemographicMetrics(age: String, gender: String, location: String, averageRating: Double, totalRating: Long)

// Spray-json format for case classes
object MyJsonProtocol extends DefaultJsonProtocol {
  implicit val movieMetricsFormat = jsonFormat5(MovieMetrics)
  implicit val genreMetricsFormat = jsonFormat3(GenreMetrics)
  implicit val demographicMetricsFormat = jsonFormat5(DemographicMetrics)
}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("MovieLensApiService")
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val BUCKET_NAME = "scala_assgn_bucket"

    val spark = SparkSession.builder()
      .appName("Movie Lens Api Service")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.driver.port", "6066")
      .master("local[*]")
      .getOrCreate()

    import MyJsonProtocol._

    // Paths to aggregated metrics
    val aggregatedDataPath = s"gs://$BUCKET_NAME/final_week_case_studies/movie_lens_analysis/output/aggregated-metrics"

    val perMovieMetricsPath = s"$aggregatedDataPath/per_movie_metrics"
    val perGenreMetricsPath = s"$aggregatedDataPath/per_genre_metrics"
    val perDemographicMetricsPath = s"$aggregatedDataPath/per_demographic_metrics"

    // Load DataFrames for each dataset
    def loadMovieMetrics(): DataFrame = spark.read.parquet(perMovieMetricsPath)
    def loadGenreMetrics(): DataFrame = spark.read.parquet(perGenreMetricsPath)
    def loadDemographicMetrics(): DataFrame = spark.read.parquet(perDemographicMetricsPath)

//    val corsSettings = CorsSettings.defaultSettings
//      .withAllowedOrigins(_ => true) // Allow all origins
//      .withAllowedMethods(scala.collection.immutable.Seq(GET, POST, PUT, DELETE, OPTIONS)) // Allow required HTTP methods
//      .withAllowCredentials(false) // If cookies/auth headers are not required, keep false
//      .withExposedHeaders(scala.collection.immutable.Seq("Content-Type", "Authorization")) // Expose necessary headers

    // CORS headers function
    def addCorsHeaders: List[HttpHeader] = List(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Methods`(akka.http.scaladsl.model.HttpMethods.GET, akka.http.scaladsl.model.HttpMethods.POST),
      `Access-Control-Allow-Headers`("Content-Type", "Authorization")
    )

    // Define routes
    val route =
      pathPrefix("api") {
        concat(
          path("movie-metrics") {
            get {
              // Fetch movie metrics and return as JSON response
              val movieMetricsDF = loadMovieMetrics()
              val movieMetrics = movieMetricsDF.sort(desc("average_rating"))
                .collect()
                .map(row => MovieMetrics(row.getAs[Int]("movieId"), row.getAs[String]("title"), row.getAs[String]("genres"), row.getAs[Double]("average_rating"), row.getAs[Long]("total_ratings")))
                .toList
                .toJson
                .prettyPrint
              respondWithHeaders(addCorsHeaders) {
                complete(OK, HttpEntity(ContentTypes.`application/json`, movieMetrics))
              }
            }
          },
          path("genre-metrics") {
            get {
              // Fetch genre metrics and return as JSON response
              val genreMetricsDF = loadGenreMetrics()
              val genreMetrics = genreMetricsDF.sort(desc("average_rating"))
                .collect()
                .map(row => GenreMetrics(row.getAs[String]("genre"), row.getAs[Double]("average_rating"), row.getAs[Long]("total_ratings")))
                .toList
                .toJson
                .prettyPrint
              respondWithHeaders(addCorsHeaders) {
                complete(OK, HttpEntity(ContentTypes.`application/json`, genreMetrics))
              }
            }
          },
          path("demographics-metrics") {
            get {
              // Fetch demographic metrics and return as JSON response
              val demographicMetricsDF = loadDemographicMetrics()
              val demographicMetrics = demographicMetricsDF.sort(desc("average_rating")).collect().map(row => DemographicMetrics(row.getAs[String]("age"), row.getAs[String]("gender"), row.getAs[String]("location"), row.getAs[Double]("average_rating"), row.getAs[Long]("total_ratings")))
                .toList
                .toJson
                .prettyPrint
              respondWithHeaders(addCorsHeaders) {
                complete(OK, HttpEntity(ContentTypes.`application/json`, demographicMetrics))
              }
            }
          }
        )
      }
    // Start the server
    val bindingFuture = Http().newServerAt("localhost", 8080).bindFlow(route)
    println("Server online at http://localhost:8080/")
    println("Press RETURN to stop...")

    scala.io.StdIn.readLine()
    bindingFuture
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }

}