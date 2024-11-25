import org.apache.spark.{SparkConf, SparkContext}

object Question4 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val conf = new SparkConf()
      .setAppName("DAG and Stages Analysis")
      .setMaster("local[*]") // Use all available cores
      .set("spark.driver.host","localhost")

    val sc = new SparkContext(conf)

    // Create an RDD of integers from 1 to 10,000
    val numbers = sc.parallelize(1 to 10000)

    // Step 1: Filter to keep only even numbers
    val evenNumbers = numbers.filter(_ % 2 == 0)

    // Step 2: Multiply each number by 10
    val multipliedNumbers = evenNumbers.map(_ * 10)

    // Step 3: Generate a tuple (remainder when divided by 100, number)
    val keyedNumbers = multipliedNumbers.map(num => (num % 100, num))

    // Step 4: ReduceByKey to group by key and compute the sum of the values
    val reducedNumbers = keyedNumbers.reduceByKey(_ + _)

    // Perform an action to collect and display the results
    val results = reducedNumbers.collect()

    // Print the results
    results.foreach(println)

    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    sc.stop()
  }
}
