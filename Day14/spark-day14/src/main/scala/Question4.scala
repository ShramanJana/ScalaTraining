import org.apache.spark.{SparkConf, SparkContext}

object Question4 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Character Frequency").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Define a collection of strings
    val stringCollection = Seq(
      "hello world",
      "spark is awesome",
      "count characters"
    )

    // Create an RDD from the collection
    val rdd = sc.parallelize(stringCollection)

    // Transform the RDD to count character frequencies
    val charFrequencies = rdd
      .flatMap(line => line.replaceAll("\\s", "").toCharArray) // Remove spaces and split into characters
      .map(char => (char, 1))                                 // Map each character to (char, 1)
      .reduceByKey(_ + _)                                     // Reduce by key to get the frequency of each character

    // Collect and print the results
    println("Character Frequencies:")
    charFrequencies.collect().foreach {
      case (char, freq) => println(s"'$char': $freq")
    }

    // Step 6: Stop the SparkContext
    sc.stop()
  }
}
