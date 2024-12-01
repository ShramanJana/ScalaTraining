package Question5

import org.apache.spark.sql.SparkSession

object ReadEnrichedData {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession with GCS configurations
    val spark = SparkSession.builder()
      .appName("Read Enriched Orders from GCS")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    // Path to the enriched orders output in GCS
    val enrichedOrdersPath = "gs://scala_assgn_bucket/day18_19Tasks/enriched_orders/"

    // Read the JSON files from the specified GCS path
    val enrichedOrdersDF = spark.read
      .json(enrichedOrdersPath)

    // Show the data
    enrichedOrdersDF.show(10)

    println("Enriched orders successfully fetched.")

    spark.stop()
  }
}
