import org.apache.spark.sql.SparkSession
import scala.util.Random

object Question1 {
  def generateDataset(args: Array[String]): Unit = {
    val spark = SparkSession.builder()
      .appName("Broadcast Join Example")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    import spark.implicits._
    // Function to generate random user details
    def generateUserDetails(numUsers: Int): Seq[(Int, String)] = {
      val random = new Random()
      (1 to numUsers).map { id =>
        (id, s"User_${random.alphanumeric.take(5).mkString}") // Random user name
      }
    }

    // Function to generate random transaction logs
    def generateTransactionLogs(numTransactions: Int, numUsers: Int): Seq[(Int, String, Double)] = {
      val random = new Random()
      (1 to numTransactions).map { _ =>
        val userId = random.nextInt(numUsers) + 1 // UserId between 1 and numUsers
        val transactionId = s"txn_${random.alphanumeric.take(6).mkString}" // Random transaction ID
        val amount = random.nextDouble() * 1000 // Random transaction amount
        (userId, transactionId, amount)
      }
    }

    // Generate random data
    val numUsers = 100       // Number of users
    val numTransactions = 10000 // Number of transactions

    val userDetailsDF = generateUserDetails(numUsers).toDF("userId", "userName")
    val transactionLogsDF = generateTransactionLogs(numTransactions, numUsers).toDF("userId", "transactionId", "amount")

    // Paths to GCS
    val userDetailsPath = "gs://scala_assgn_bucket/day18_19Tasks/user_details.csv"
    val transactionLogsPath = "gs://scala_assgn_bucket/day18_19Tasks/transaction_logs.csv"

    // Write data to GCS
    userDetailsDF.write
      .option("header", "true")
      .csv(userDetailsPath)

    transactionLogsDF.write
      .option("header", "true")
      .csv(transactionLogsPath)

    println(s"Data successfully written to GCS at $userDetailsPath and $transactionLogsPath")

    // Stop Spark session
    spark.stop()
  }

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder()
      .appName("Broadcast Join Example")
      .config("spark.hadoop.fs.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFileSystem")
      .config("spark.hadoop.fs.AbstractFileSystem.gs.impl", "com.google.cloud.hadoop.fs.gcs.GoogleHadoopFS")
      .config("spark.hadoop.google.cloud.auth.service.account.enable", "true")
      .config("spark.hadoop.google.cloud.auth.service.account.json.keyfile", "/Users/shramanjana/gcp-final-key.json")
      .master("local[*]")
      .getOrCreate()

    // Paths to GCS
    val userDetailsPath = "gs://scala_assgn_bucket/day18_19Tasks/user_details.csv"
    val transactionLogsPath = "gs://scala_assgn_bucket/day18_19Tasks/transaction_logs.csv"

    val userDetails = spark.read
      .option("header", "true") // Adjusted for datasets saved with headers
      .option("inferSchema", "true")
      .csv(userDetailsPath)
      .toDF("user_id", "name") // Rename columns for consistency if necessary

    val transactionLogs = spark.read
      .option("header", "true") // Adjusted for datasets saved with headers
      .option("inferSchema", "true")
      .csv(transactionLogsPath)
      .toDF("user_id", "transaction_type", "amount")

    // Use broadcasting for the small dataset
    val joinedDataDF = transactionLogs
      .join(org.apache.spark.sql.functions.broadcast(userDetails), Seq("user_Id"))

    // Show results
    println("Joined Data:")
    joinedDataDF.show(10)

    // Stop Spark session
    spark.stop()
  }
}