import org.apache.spark.{SparkConf, SparkContext}

object Question2 {
  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Cartesian Product").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create two RDDs with numbers
    val rdd1 = sc.parallelize(Seq(1, 2, 3))
    val rdd2 = sc.parallelize(Seq(4, 5, 6))

    // Compute the Cartesian product of the two RDDs
    val cartesianProduct = rdd1.cartesian(rdd2)

    // Collect and print the results
    println("Cartesian Product:")
    cartesianProduct.collect().foreach(println)

    // Stop the SparkContext
    sc.stop()
  }
}
