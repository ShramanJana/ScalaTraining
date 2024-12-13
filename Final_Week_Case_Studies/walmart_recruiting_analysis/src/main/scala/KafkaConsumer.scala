import AppConstants.{BUCKET_NAME, KAFKA_BROKERS, KAFKA_TOPIC}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{Dataset, Row, SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel

import java.util.concurrent.TimeUnit
import scala.util.Random

object KafkaConsumer {
  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Walmart Real Time Sales Data Pipeline ")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.driver.port", "6066")
      .master("local[*]")
      .getOrCreate()


    val trainSchema = StructType(Seq(
      StructField("Store", IntegerType, nullable = false),
      StructField("Dept", IntegerType, nullable = false),
      StructField("Date", StringType, nullable = false),
      StructField("Weekly_Sales", DoubleType, nullable = false),
      StructField("IsHoliday", BooleanType, nullable = false)
    ))

    val featuresSchema = StructType(Seq(
      StructField("Store", IntegerType),
      StructField("Date", StringType),
      StructField("Temperature", DoubleType),
      StructField("Fuel_Price", DoubleType),
      StructField("CPI", DoubleType),
      StructField("Unemployment", DoubleType)
    ))

    val storesSchema = StructType(Seq(
      StructField("Store", IntegerType),
      StructField("Type", StringType),
      StructField("Size", IntegerType)
    ))

    // Dataset paths
    val featuresDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/features.csv"
    val storesDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/stores.csv"
    val enrichedDataOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/enriched_data/"
    val storeMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/store_metrics/"
    val deptMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/dept_metrics/"
    val holidayMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/holiday_metrics/"

    // Starting Kafka Consumer
    val kafkaSource = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", KAFKA_BROKERS) // Update with your Kafka server
      .option("subscribe", KAFKA_TOPIC) // Kafka topic
      .option("startingOffsets", "latest") // Start reading from latest messages
      .load()

    // Parse Kafka messages (JSON) and extract fields
    val parsedStreamDF = kafkaSource
      .selectExpr("CAST(value AS STRING) as jsonString")
      .select(from_json(col("jsonString"), trainSchema).as("data"))
      .select("data.*") // Flatten JSON structure

    val query = parsedStreamDF.writeStream
      .foreachBatch { (batchDf: Dataset[Row], _: Long) =>
        val persistedBatchDf = batchDf.persist(StorageLevel.MEMORY_AND_DISK) // persisted with replication
        persistedBatchDf.show(5, truncate = false)


        val featuresDf = spark.read.option("header", "true").schema(featuresSchema).csv(featuresDatasetPath)
        val storesDf = spark.read.option("header", "true").schema(storesSchema).csv(storesDatasetPath)

        // Data validation
        val cleanedFeaturesDf = featuresDf.na.drop().cache() // removed the missing values and cached the features dataset
        val cleanedStoresDf = broadcast(storesDf.na.drop()) // removed the missing values and broadcast the stores dataset (size is small 45 rows)

        // enrich train data with features and stores data, we are doing inner join to remove the rows with missing metadata
        val newEnrichedData = persistedBatchDf
          .repartition(col("Store"), col("Date"))
          .join(cleanedFeaturesDf, Seq("Store", "Date"), "inner") // inner join with Features data on "Store" and "Date"
          .join(cleanedStoresDf, Seq("Store"), "inner") //inner join with Store data on "Store" column

        // write enriched
        // append new data to the enriched path
        newEnrichedData.write.mode(SaveMode.Append).partitionBy("Store", "Date").parquet(enrichedDataOutputPath)
        println("Successfully saved new enriched data to GCP bucket")

        val enrichedData = spark.read
          .option("header", "true") // Adjusted for datasets saved with headers
          .option("inferSchema", "true")
          .option("basePath", enrichedDataOutputPath)
          .parquet(enrichedDataOutputPath)


        // Aggregations
        // Store level
        val storeMetrics = enrichedData.groupBy("Store").agg(
          sum("Weekly_Sales").as("Total_Weekly_Sales"),
          avg("Weekly_Sales").as("Avg_Weekly_Sales")
        ).orderBy(desc("Total_Weekly_Sales")).cache()

        storeMetrics.limit(100).write.mode(SaveMode.Overwrite).json(storeMetricsOutputPath)
        println("Successfully updated store metrics data to GCP bucket")
        storeMetrics.unpersist()

        // Department level
        // Define a window specification to group by Department and order by Date
        val windowSpec = Window.partitionBy("Dept").orderBy("Date")

        val deptMetrics = enrichedData.groupBy(col("Dept"), col("Date")).agg(
            sum("Weekly_Sales").as("Total_Weekly_Sales")
          )
          .withColumn("Prev_Weekly_Sales", lag("Total_Weekly_Sales", 1).over(windowSpec)) // Get previous week's sales
          .withColumn("Weekly_Difference", col("Total_Weekly_Sales") - col("Prev_Weekly_Sales")) // Calculate difference
          .orderBy("Dept", "Date").cache()

        deptMetrics.limit(100).write.mode(SaveMode.Overwrite).json(deptMetricsOutputPath)
        println("Successfully updated department metrics data to GCP bucket")
        deptMetrics.unpersist()

        val holidayMetrics = enrichedData.groupBy("Dept", "IsHoliday")
          .agg(
            sum("Weekly_Sales").as("Total_Weekly_Sales"),
            avg("Weekly_Sales").as("Average_Weekly_Sales")
          ).withColumn(
            "Day_Type", when(col("IsHoliday"), "Holiday").otherwise("Workday") // Creating new column to denote the day type (workday/holiday)
          )
          .drop("IsHoliday")
          .orderBy("Dept")
          .cache()

        holidayMetrics.limit(100).write.mode(SaveMode.Overwrite).json(holidayMetricsOutputPath)
        println("Successfully updated holiday metrics data to GCP bucket")
        holidayMetrics.unpersist()

        enrichedData.unpersist()
        batchDf.unpersist()
        println("Updation of metrics completed")
      }
      .trigger(Trigger.ProcessingTime(60, TimeUnit.SECONDS)) // processing the data every 60 seconds
      .start()

    query.awaitTermination()

    //    val query = parsedStreamDF.writeStream
    //      .foreachBatch{ (batchDf: Dataset[Row], _: Long) =>
    //        val persistedBatchDf = batchDf.persist(StorageLevel.MEMORY_AND_DISK)
    //        persistedBatchDf.show(5)
    //
    //        // Schema definitions
    //        val featuresSchema = StructType(Seq(
    //          StructField("Store", IntegerType),
    //          StructField("Date", StringType),
    //          StructField("Temperature", DoubleType),
    //          StructField("Fuel_Price", DoubleType),
    //          StructField("CPI", DoubleType),
    //          StructField("Unemployment", DoubleType)
    //        ))
    //
    //        val storesSchema = StructType(Seq(
    //          StructField("Store", IntegerType),
    //          StructField("Type", StringType),
    //          StructField("Size", IntegerType)
    //        ))
    //
    //        // Dataset paths
    //        val featuresDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/features.csv"
    //        val storesDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/stores.csv"
    //
    //        val featuresDf = spark.read.option("header", "true").schema(featuresSchema).csv(featuresDatasetPath)
    //        val storesDf = spark.read.option("header", "true").schema(storesSchema).csv(storesDatasetPath)
    //
    //        // Data validation
    //        val cleanedFeaturesDf = featuresDf.na.drop().cache()
    //        val cleanedStoresDf = broadcast(storesDf.na.drop())
    //
    //        // Repartition before join
    //        val newEnrichedData = persistedBatchDf
    //          .join(cleanedFeaturesDf, Seq("Store", "Date"), "inner")
    //          .join(cleanedStoresDf, Seq("Store"), "inner")
    //          .repartition(col("Store"), col("Date"))
    //
    //        // Write enriched data to GCS
    //        val enrichedDataOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/enriched_data/"
    //        // append new data to the enriched path
    //        newEnrichedData.write.mode(SaveMode.Append).partitionBy("Store", "Date").parquet(enrichedDataOutputPath)
    //
    //        val enrichedData = spark.read.option("header", "true").parquet(enrichedDataOutputPath)
    //        // Aggregations (Store, Dept, Holiday)
    //        val storeMetrics = enrichedData.groupBy("Store").agg(
    //          sum("Weekly_Sales").as("Total_Weekly_Sales"),
    //          avg("Weekly_Sales").as("Avg_Weekly_Sales")
    //        ).orderBy(desc("Total_Weekly_Sales"))
    //
    //        val storeMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/store_metrics/"
    //        storeMetrics.write.mode(SaveMode.Overwrite).json(storeMetricsOutputPath)
    //
    //        val deptMetrics = enrichedData.groupBy(col("Dept"), col("Date")).agg(
    //          sum("Weekly_Sales").as("Total_Weekly_Sales")
    //        ).withColumn("Prev_Weekly_Sales", lag("Total_Weekly_Sales", 1).over(Window.partitionBy("Dept").orderBy("Date")))
    //
    //        val deptMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/dept_metrics/"
    //        deptMetrics.write.mode(SaveMode.Overwrite).json(deptMetricsOutputPath)
    //
    //        val holidayMetrics = enrichedData.groupBy("Dept", "IsHoliday").agg(
    //          sum("Weekly_Sales").as("Total_Weekly_Sales"),
    //          avg("Weekly_Sales").as("Average_Weekly_Sales")
    //        ).withColumn("Day_Type", when(col("IsHoliday"), "Holiday").otherwise("Workday"))
    //
    //        val holidayMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/holiday_metrics/"
    //        holidayMetrics.write.mode(SaveMode.Overwrite).json(holidayMetricsOutputPath)
    //
    //        enrichedData.unpersist()
    //        batchDf.unpersist()
    //        println("Updation of metrics completed")
    //      }
    //      .trigger(Trigger.ProcessingTime(60, TimeUnit.SECONDS)) // Adjust processing time
    //      .start()
    //
    //    query.awaitTermination()

    spark.stop()
  }
}
