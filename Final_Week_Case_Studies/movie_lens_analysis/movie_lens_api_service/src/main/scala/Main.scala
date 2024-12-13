import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpHeader
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.desc

import javax.ws.rs._
import scala.concurrent.ExecutionContextExecutor

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
      .master("local[*]")
      .getOrCreate()

    // Paths to aggregated metrics
    val aggregatedDataPath = s"gs://$BUCKET_NAME/final_week_case_studies/movie_lens_analysis/output/aggregated-metrics"

    val perMovieMetricsPath = s"$aggregatedDataPath/per_movie_metrics"
    val perGenreMetricsPath = s"$aggregatedDataPath/per_genre_metrics"
    val perDemographicMetricsPath = s"$aggregatedDataPath/per_demographic_metrics"

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
              val movieMetricsDF = spark.read.parquet(perMovieMetricsPath)
              val movieMetrics = movieMetricsDF.sort(desc("average_rating")).collect().map(_.toString()).mkString("\n")
              respondWithHeaders(addCorsHeaders) {
                complete(movieMetrics)
              }
            }
          },
          path("genre-metrics") {
            get {
              val genreMetricsDF = spark.read.parquet(perGenreMetricsPath)
              val genreMetrics = genreMetricsDF.sort(desc("average_rating")).collect().map(_.toString()).mkString("\n")

              respondWithHeaders(addCorsHeaders) {
                complete(genreMetrics)
              }
            }
          },
          path("demographics-metrics") {
            get {
              val demographicMetricsDF = spark.read.parquet(perDemographicMetricsPath)
              val demographicMetrics = demographicMetricsDF.sort(desc("average_rating")).collect().map(_.toString()).mkString("\n")

              respondWithHeaders(addCorsHeaders) {
                complete(demographicMetrics)
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