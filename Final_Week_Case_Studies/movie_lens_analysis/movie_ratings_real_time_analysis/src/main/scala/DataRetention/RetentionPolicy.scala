package DataRetention

import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.spark.sql.SparkSession

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.{Executors, TimeUnit}

object RetentionPolicy {

  // Date format for partitioned folders
  val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  val daysThreshold = 14

  def main(args: Array[String]): Unit = {
    // Initialize Spark session
    val spark = SparkSession.builder()
      .appName("Movie Ratings Retention")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.driver.port", "6066")
      .master("local[*]")
      .getOrCreate()

    val basePath = s"gs://scala_assgn_bucket/final_week_case_studies/movie_lens_analysis/output/ratingEnriched/"

    // Schedule the task to run once a day
    val scheduler = Executors.newScheduledThreadPool(1)
    scheduler.scheduleAtFixedRate(() =>
      runCleanupJob(spark, basePath),
      0, // Initial delay (immediate)
      1, // Period
      TimeUnit.DAYS // Time unit
    )

    println("Scheduled job started")

    // Block the main thread
    sys.addShutdownHook {
      scheduler.shutdown()
      spark.stop()
      println("Scheduler stopped.")
    }
  }

  def runCleanupJob(spark: SparkSession, basePath: String): Unit = {

    // Calculate threshold date
    val thresholdDate = LocalDate.now().minusDays(daysThreshold)

    // Initialize Hadoop FileSystem for GCS
    val fs = FileSystem.get(spark.sparkContext.hadoopConfiguration)

    println(s"Starting cleanup for files older than $thresholdDate...")

    // List files recursively in the base directory
    val files = listFiles(fs, new Path(basePath))

    // Filter files/directories older than the threshold date
    val filesToDelete = files.filter { filePath =>
      val dateFromPath = extractDateFromPath(filePath)
      dateFromPath.exists(_.isBefore(thresholdDate))
    }

    // Delete files/directories
    filesToDelete.foreach { file =>
      println(s"Deleting: $file")
      fs.delete(new Path(file), true) // Recursively delete files/directories
    }

    println("Cleanup completed.")

    // Stop Spark session
    spark.stop()

  }

  /**
   * List all files/directories recursively in the specified path.
   */
  def listFiles(fs: FileSystem, path: Path): Seq[String] = {
    val status = fs.listStatus(path)
    status.flatMap { fileStatus =>
      val filePath = fileStatus.getPath.toString
      if (fileStatus.isDirectory) {
        listFiles(fs, fileStatus.getPath)
      } else {
        Seq(filePath)
      }
    }
  }

  /**
   * Extract date from the folder structure (e.g., /path/date=2024-12-13/).
   */
  def extractDateFromPath(path: String): Option[LocalDate] = {
    val datePattern = """date=(\d{4}-\d{2}-\d{2})""".r
    datePattern.findFirstMatchIn(path).flatMap { matchData =>
      val dateString = matchData.group(1)
      try {
        Some(LocalDate.parse(dateString, dateFormatter))
      } catch {
        case _: Exception => None // Invalid date format
      }
    }
  }

}
