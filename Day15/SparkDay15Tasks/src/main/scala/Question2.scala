import org.apache.spark.{SparkConf, SparkContext}

object Question2 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val conf = new SparkConf()
      .setAppName("Narrow and Wide Transformations")
      .setMaster("local[*]") // Use all available cores
      .set("spark.driver.host","localhost")

    val sc = new SparkContext(conf)

    // Create an RDD of numbers from 1 to 1000
    val numbers = sc.parallelize(1 to 1000)

    // Narrow transformation: map
    val squaredNumbers = numbers.map(x => x * x)

    // Narrow transformation: filter
    val filteredNumbers = squaredNumbers.filter(x => x % 2 == 0)

    // Wide transformation: Convert numbers into key-value pairs and reduce by key
    val keyValuePairs = filteredNumbers.map(x => (x % 10, x))

    // Apply reduceByKey to sum numbers with the same key
    val reducedByKey = keyValuePairs.reduceByKey(_ + _)

    // Save the results to a text file
    reducedByKey.saveAsTextFile("output/reduced_by_key")

    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    sc.stop()

  }
}
