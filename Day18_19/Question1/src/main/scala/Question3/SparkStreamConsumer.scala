import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.types._

object SparkStreamConsumer {

  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Kafka Structured Streaming")
      .master("local[*]") // Use local for testing
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    val kafkaSource = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092") // Update with your Kafka server
      .option("subscribe", "transactions") // Kafka topic
      .option("startingOffsets", "latest") // Start reading from latest messages
      .load()

    // Parse JSON messages and extract required fields
    val transactionSchema = StructType(Seq(
      StructField("transactionId", StringType, nullable = false),
      StructField("userId", StringType, nullable = false),
      StructField("amount", DoubleType, nullable = false),
    ))

    val parsedMessages = kafkaSource.selectExpr("CAST(value AS STRING) as jsonString", "timestamp")
      .select(from_json(col("jsonString"), transactionSchema).as("data"), col("timestamp"))
      .select("data.*", "timestamp")

    // Perform windowed aggregation to calculate the total amount in a 10-second window
    // Add watermark and perform aggregation
    val windowedAggregation = parsedMessages
      .withWatermark("timestamp", "10 seconds") // Watermark based on timestamp column
      .groupBy(
        window(col("timestamp"), "10 seconds") // Use windowing for aggregation
      )
      .agg(sum("amount").as("totalAmount"))

    // Write the results to the console
    val query = windowedAggregation.writeStream
      .outputMode("update") // Use "update" for incremental results
      .format("console")
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .option("truncate", false) // Prevent truncation of output for readability
      .start()

    query.awaitTermination()
  }

}
