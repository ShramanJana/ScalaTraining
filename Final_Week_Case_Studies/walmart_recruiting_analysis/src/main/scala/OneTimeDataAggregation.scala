import AppConstants.{BUCKET_NAME, KAFKA_BROKERS, KAFKA_TOPIC}
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.storage.StorageLevel

object OneTimeDataAggregation {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Walmart Data Pipeline")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .config("spark.driver.bindAddress", "127.0.0.1")
      .config("spark.driver.port", "6066")
      .master("local[*]")
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    // Load data
    // Define the schema for the csv files
    val trainSchema = StructType(Seq(
      StructField("Store", IntegerType),
      StructField("Dept", IntegerType),
      StructField("Date", StringType),
      StructField("Weekly_Sales", DoubleType),
      StructField("IsHoliday", BooleanType)
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
    val trainDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/train.csv"
    val featuresDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/features.csv"
    val storesDatasetPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/Input_Datasets/stores.csv"

    val trainDf = spark.read.option("header", "true").schema(trainSchema).csv(trainDatasetPath)
    val featuresDf = spark.read.option("header", "true").schema(featuresSchema).csv(featuresDatasetPath)
    val storesDf = spark.read.option("header", "true").schema(storesSchema).csv(storesDatasetPath)

    // Data validation
    val validTrainDf = trainDf.filter("Weekly_Sales >= 0").na.drop()
    val cleanedFeaturesDf = featuresDf.na.drop().cache() // removed the missing values and cached the features dataset
    val cleanedStoresDf = broadcast(storesDf.na.drop()) // removed the missing values and broadcast the stores dataset (size is small 45 rows)
    println("Successfully cleaned Data")

    // enrich train data with features and stores data, we are doing inner join to remove the rows with missing metadata
    val enrichedData = validTrainDf
      .join(cleanedFeaturesDf, Seq("Store", "Date"), "inner") // inner join with Features data on "Store" and "Date"
      .join(cleanedStoresDf, Seq("Store"), "inner") //inner join with Store data on "Store" column
      .repartition(col("Store"), col("Date"))
      .persist(StorageLevel.MEMORY_AND_DISK) // save in diskmemory not available if memory not available and with replication factor 2
    println("Enriched train data with features and stores")
    enrichedData.show(10)

    // write enriched
    val enrichedDataOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/enriched_data/"
    enrichedData.limit(100).write.mode(SaveMode.Overwrite).partitionBy("Store", "Date").parquet(enrichedDataOutputPath)
    println("Successfully written enriched data to GCP bucket")

    // Aggregations
    // Store level
    val storeMetrics = enrichedData.groupBy("Store").agg(
      sum("Weekly_Sales").as("Total_Weekly_Sales"),
      avg("Weekly_Sales").as("Avg_Weekly_Sales")
    ).orderBy(desc("Total_Weekly_Sales")).cache()
    storeMetrics.show(10)

    val storeMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/store_metrics/"
    storeMetrics.limit(100).write.mode(SaveMode.Overwrite).json(storeMetricsOutputPath)
    println("Successfully written store metrics data to GCP bucket")
    storeMetrics.unpersist()

    // Department level
    // Define a window specification to group by Department and order by Date
    val windowSpec = Window.partitionBy("Dept").orderBy("Date")

    val deptMetrics = enrichedData.groupBy(col("Dept"), col("Date")).agg(
        sum("Weekly_Sales").as("Total_Weekly_Sales")
      ).withColumn("Prev_Weekly_Sales", lag("Total_Weekly_Sales", 1).over(windowSpec)) // Get previous week's sales
      .withColumn("Weekly_Difference", col("Total_Weekly_Sales") - col("Prev_Weekly_Sales")) // Calculate difference
      .orderBy("Dept", "Date").cache()
    deptMetrics.show(10)

    val deptMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/dept_metrics/"
    deptMetrics.limit(100).write.mode(SaveMode.Overwrite).json(deptMetricsOutputPath)
    println("Successfully written department metrics data to GCP bucket")
    deptMetrics.unpersist()

    // holiday vs Non-Holiday metrics
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
    holidayMetrics.show(10)

    val holidayMetricsOutputPath = s"gs://$BUCKET_NAME/final_week_case_studies/Walmart_Dataset_Analysis/output/holiday_metrics/"
    holidayMetrics.limit(100).write.mode(SaveMode.Overwrite).json(holidayMetricsOutputPath)
    println("Successfully written holiday metrics data to GCP bucket")
    holidayMetrics.unpersist()

    enrichedData.unpersist()

    spark.stop()
  }
}