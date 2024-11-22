import org.apache.spark.{SparkConf, SparkContext}

object Question7 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Union and Distinct").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create two RDDs of integers
    val rdd1 = sc.parallelize(Seq(1, 2, 3, 4))
    val rdd2 = sc.parallelize(Seq(3, 4, 5, 6))

    // Perform union operation on the two RDDs
    val unionRDD = rdd1.union(rdd2)

    // Remove duplicate elements using the distinct transformation
    val distinctRDD = unionRDD.distinct()

    // Collect and print the results
    println("Union RDD with distinct elements:")
    distinctRDD.collect().foreach(println)
    sc.stop()
  }
}
