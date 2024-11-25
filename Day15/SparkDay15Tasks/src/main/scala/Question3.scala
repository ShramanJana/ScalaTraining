import org.apache.spark.{SparkConf, SparkContext}

object Question3 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val conf = new SparkConf()
      .setAppName("Task Distribution in Local Mode")
      .setMaster("local[*]") // Use all available cores
      .set("spark.driver.host","localhost")

    val sc = new SparkContext(conf)

    val lines = sc.parallelize(Seq.fill(1000000)("lorem ipsum dolor sit amet consectetur adipiscing elit"), 24)

    // Step 1: Split each string into words
    val words = lines.flatMap(line => line.split(" "))

    // Step 2: Map each word to a (word, 1) pair
    val wordPairs = words.map(word => (word, 1))

    // Step 3: Reduce by key to count word occurrences
    val wordCounts = wordPairs.reduceByKey(_ + _)

    // Collect and print a sample of the results
    wordCounts.take(10).foreach(println)

    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    sc.stop()
  }
}
