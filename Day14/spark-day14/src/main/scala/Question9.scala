import org.apache.spark.{SparkConf, SparkContext}

object Question9 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Sum of Integers").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create an RDD of integers from 1 to 100
    val rdd = sc.parallelize(1 to 100)

    // Compute the sum using the reduce action
    val sum = rdd.fold(0)(_ + _)

    // Print the result
    println(s"Sum of integers from 1 to 100: $sum")

    // Stop the SparkContext
    sc.stop()
  }
}
