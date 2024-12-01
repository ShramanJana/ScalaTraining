import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.col

import scala.util.Random

object Question4 {
  def generateDataset(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Generate Data GCP Cloud")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    // Paths to GCS
    val gcsPath = "gs://scala_assgn_bucket/day18_19Tasks/question4_input.csv"

    import spark.implicits._

    val random = new Random()
    val data = (1 to 10000).map { id =>
      val status = if(random.nextBoolean()) "completed" else "pending"
      val amount = random.nextDouble() * 1000 // Random transaction amount
      (id, status, amount)
    }

    val df = data.toDF("id", "status", "amount")

    // Write DataFrame as a Parquet file to GCS
    df.write
      .mode("overwrite") // Overwrite existing data
      .parquet(gcsPath)

    println(s"Parquet file with 10,000 rows successfully written to $gcsPath")

    // Stop SparkSession
    spark.stop()
  }

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Filter Data GCP Cloud")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    // Paths to GCS
    val gcsInputPath = "gs://scala_assgn_bucket/day18_19Tasks/question4_input.csv"
    val gcsOutputPath = "gs://scala_assgn_bucket/day18_19Tasks/question4_output.csv"

    // Read Parquet file from GCS
    val transactionInputDF = spark.read.parquet(gcsInputPath)

    // Process data: Filter rows where status = "completed"
    val processedData = transactionInputDF.filter(col("status") === "completed")

    // Write the processed data back to GCS in Parquet format
    processedData.write.mode("overwrite").parquet(gcsOutputPath)

    println(s"Processed data successfully written to $gcsOutputPath")

    // Stop SparkSession
    spark.stop()


  }
}
