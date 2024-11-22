import org.apache.spark.{SparkConf, SparkContext}

object Question6 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Join RDDs").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create two RDDs with key-value pairs (id, name) and (id, score)
    val namesRDD = sc.parallelize(Seq((1, "Alice"), (2, "Bob"), (3, "Charlie"), (4, "David")))
    val scoresRDD = sc.parallelize(Seq((1, 85.0), (2, 90.0), (3, 78.0), (5, 88.0)))

    // Perform an inner join on the two RDDs based on the key (id)
    val joinedRDD = namesRDD.join(scoresRDD)

    // Transform the result to produce (id, name, score)
    val resultRDD = joinedRDD.map {
      case (id, (name, score)) => (id, name, score)
    }

    // Collect and print the results
    println("Joined RDD (id, name, score):")
    resultRDD.collect().foreach(println)

    sc.stop()
  }
}
