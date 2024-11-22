import org.apache.spark.{SparkConf, SparkContext}

object Question1 {
  def main(args: Array[String]): Unit = {
    // Step 1: Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Word Count").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Step 2: Define a collection of strings
    val stringCollection = Seq(
      "Apache Spark is fast",
      "Big data processing with Spark",
      "Count the total number of words"
    )

    // Step 3: Parallelize the collection to create an RDD
    val rdd = sc.parallelize(stringCollection)

    // Step 4: Transform the RDD to count the total number of words
    val totalWords = rdd
      .flatMap(line => line.split("\\s+")) // Split each line into words
      .filter(word => word.nonEmpty)      // Filter out empty words
      .count()                            // Count the total number of words

    // Step 5: Print the total word count
    println(s"Total number of words: $totalWords")

    // Stop the SparkContext
    sc.stop()
  }
}