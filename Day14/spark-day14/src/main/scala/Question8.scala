import org.apache.spark.{SparkConf, SparkContext}

object Question8 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Filter by Age").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create an RDD from a list of CSV strings
    val csvData = Seq(
      "1,John,25",
      "2,Emma,17",
      "3,Liam,30",
      "4,Olivia,15",
      "5,Noah,20"
    )
    val rdd = sc.parallelize(csvData)

    // Parse the CSV rows and filter records where age is 18 or older
    val filteredRDD = rdd
      .map(line => line.split(",")) // Split each line by commas
      .filter(fields => fields.length == 3 && fields(2).toInt >= 18) // Check age >= 18
      .map(fields => (fields(0).toInt, fields(1), fields(2).toInt)) // Convert to (id, name, age)

    // Collect and print the filtered records
    println("Filtered Records (id, name, age):")
    filteredRDD.collect().foreach(println)
    sc.stop()
  }
}
