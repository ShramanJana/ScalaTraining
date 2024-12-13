import AppConstants.{BUCKET_NAME, KAFKA_BROKERS, KAFKA_TOPIC}
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.Trigger

import java.util.concurrent.TimeUnit

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Movie Ratings With Enrichment")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Schema for the incoming movie ratings JSON
    val movieRatingSchema = new StructType()
      .add("userId", IntegerType)
      .add("movieId", IntegerType)
      .add("rating", DoubleType)
      .add("timestamp", LongType)

    // Read data from Kafka topic
    val rawRatingsDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BROKERS)
      .option("subscribe", KAFKA_TOPIC)
      .option("startingOffsets", "latest")
      .load()

    // Deserialize Kafka message values as JSON
    val ratingsDF = rawRatingsDF.selectExpr("CAST(value AS STRING) as jsonData")
      .select(from_json(col("jsonData"), movieRatingSchema).as("data"))
      .select("data.*")

    // Write the enriched data to Parquet format, partitioned by date
    val parquetSinkPath = s"gs://$BUCKET_NAME/final_week_case_studies/movie_lens_analysis/output/ratingEnriched/"

    // Incremental Aggregation Setup
    val aggregatedDataPath = s"gs://$BUCKET_NAME/final_week_case_studies/movie_lens_analysis/output/aggregated-metrics"

    val perMovieMetricsPath = s"$aggregatedDataPath/per_movie_metrics"
    val perGenreMetricsPath = s"$aggregatedDataPath/per_genre_metrics"
    val perDemographicMetricsPath = s"$aggregatedDataPath/per_demographic_metrics"

    ratingsDF.writeStream.foreachBatch { (batchDF: Dataset[Row], batchId: Long) =>

        // Read movie metadata from CSV
        val moviesDF = spark.read
          .option("header", "true")
          .csv(s"gs://$BUCKET_NAME/final_week_case_studies/movie_lens_analysis/input_datasets/movies.csv")
          .selectExpr("cast(movieId as int) as movieId", "title", "genres").cache()

        // Read user demographics from CSV
        val usersDF = spark.read
          .option("header", "true")
          .csv(s"gs://$BUCKET_NAME/final_week_case_studies/movie_lens_analysis/input_datasets/users.csv")
          .selectExpr("cast(userId as int) as userId", "firstName", "lastName", "age", "location", "gender").cache()

        // Validate ratings (between 0.5 and 5.0)
        val validatedRatingsDF = batchDF.filter(col("rating").between(0.5, 5.0))

        // Enrich ratings with movie metadata
        val enrichedWithMoviesDF = validatedRatingsDF.join(moviesDF, "movieId")

        // Enrich ratings with user demographics
        val fullyEnrichedDF = enrichedWithMoviesDF.join(usersDF, "userId")

        println("Enriched the rating data with movie and users")
        // Add a new column for partitioning by date
        val enrichedWithDateDF = fullyEnrichedDF.withColumn("date", to_date(from_unixtime(col("timestamp") / 1000)))
        enrichedWithDateDF.show(10, truncate = false)

        // append new data to the enriched path
        enrichedWithDateDF.limit(100).write.mode(SaveMode.Append).partitionBy("date").parquet(parquetSinkPath)
        println("Successfully saved new enriched data to GCP bucket")

        val enrichedData = spark.read
          .option("header", "true") // Adjusted for datasets saved with headers
          .option("inferSchema", "true")
          .option("basePath", parquetSinkPath)
          .parquet(parquetSinkPath)

        // Aggregation Per Movie
        val perMovieMetricsDF = enrichedData.groupBy("movieId", "title", "genres").agg(
          avg("rating").as("average_rating"),
          count("rating").as("total_ratings")
        )
        perMovieMetricsDF.show(10, truncate = false)
        perMovieMetricsDF.limit(100).write.mode("overwrite").parquet(perMovieMetricsPath)
        println("Successfully updated per_movie metrics data to GCP bucket")

        // Aggregation Per Genre
        val perGenreMetricsDF = enrichedData.withColumn("genre", explode(split(col("genres"), "\\|"))) // Split and explode genres
          .groupBy("genre")
          .agg(
            avg("rating").as("average_rating"),
            count("rating").as("total_ratings")
          )
        perGenreMetricsDF.show(10, truncate = false)
        perGenreMetricsDF.limit(100).write.mode("overwrite").parquet(perGenreMetricsPath)
        println("Successfully updated per_genre metrics data to GCP bucket")

        // Aggregation Per User Demographic
        val perDemographicMetricsDF = enrichedData.groupBy("age", "gender", "location")
          .agg(
            avg("rating").as("average_rating"),
            count("rating").as("total_ratings")
          )
        perDemographicMetricsDF.show(10, truncate = false)
        perDemographicMetricsDF.limit(100).write.mode("overwrite").parquet(perDemographicMetricsPath)
        println("Successfully updated per_user_demographic metrics data to GCP bucket")

        println(s"Successfully updated aggregation metrics for batchID $batchId")
      }.trigger(Trigger.ProcessingTime(100, TimeUnit.SECONDS)) // processing the data every 60 seconds
      .start().awaitTermination()

    spark.stop()
  }
}