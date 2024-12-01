import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, expr, struct, to_json}
import org.apache.spark.sql.streaming.Trigger

object KafkaProducer {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Broadcast Join Example")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    val transactionLogsPath = "gs://scala_assgn_bucket/day18_19Tasks/transaction_logs.csv"

    // Read the CSV file as a static DataFrame
    val transactionDF = spark.read
      .option("header", "true") // Adjusted for datasets saved with headers
      .option("inferSchema", "true")
      .csv(transactionLogsPath)
      .toDF("userId", "transactionId", "amount")

    // Convert the static DataFrame into a streaming DataFrame by attaching a rate stream
    val rateStream = spark.readStream
      .format("rate")
      .option("rowsPerSecond", 1) // Emit one row per second
      .load()

    // Attach an index to simulate streaming from the static DataFrame
    val indexedCsvDF = transactionDF.withColumn("index", expr("row_number() over (order by userId) - 1"))

    // Join the rate stream with the static DataFrame to emit one row per second
    val streamingDF = rateStream
      .withColumn("index", col("value"))
      .join(indexedCsvDF, "index")
      .select(col("userId").cast("string").as("key"), to_json(struct("userId", "transactionId", "amount")).as("value")) // Kafka expects key and value as strings

    // Write the streaming data to Kafka
    val kafkaSink = streamingDF.writeStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092") // Update with your Kafka server
      .option("topic", "transactions") // Update with your Kafka topic
      .option("checkpointLocation", "/tmp/spark-kafka-checkpoints") // Directory for checkpointing
      .trigger(Trigger.ProcessingTime("1 second")) // Emit rows at a 1-second interval
      .start()

    kafkaSink.awaitTermination()
  }
}
