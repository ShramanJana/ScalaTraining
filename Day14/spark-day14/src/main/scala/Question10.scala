import org.apache.spark.{SparkConf, SparkContext}

object Question10 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Group By Key and Sum").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create an RDD of key-value pairs
    val data = Seq(("a", 10), ("b", 20), ("a", 5), ("b", 15), ("c", 25))
    val rdd = sc.parallelize(data)

    // Group by key and compute the sum of values for each key
    val resultRDD = rdd
      .groupByKey() // Group values by key
      .mapValues(values => values.sum) // Compute sum of values for each key

    // Collect and print the results
    println("Sum of values for each key:")
    resultRDD.collect().foreach(println)
    sc.stop()
  }
}
