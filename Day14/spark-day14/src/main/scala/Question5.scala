import org.apache.spark.{SparkConf, SparkContext}

object Question5 {
  def main(args: Array[String]): Unit = {
    // Initialize SparkConf and SparkContext
    val conf = new SparkConf().setAppName("Average Score Calculation").setMaster("local[*]")
    val sc = new SparkContext(conf)

    // Create an RDD from a list of tuples (id, score)
    val data = Seq((1, 85.0), (2, 90.0), (3, 78.0), (4, 92.0), (5, 88.0))
    val rdd = sc.parallelize(data)

    // Calculate the total score and the count of records
    val (totalScore, count) = rdd
      .map { case (_, score) => (score, 1) }
      .aggregate((0.0, 0))(
        (acc, value) => (acc._1 + value._1, acc._2 + value._2), // SeqOp
        (acc1, acc2) => (acc1._1 + acc2._1, acc1._2 + acc2._2)  // CombOp
      )
    val averageScore = totalScore / count

    // Print the average score
    println(f"Average Score: $averageScore%.2f")

    // Stop the SparkContext
    sc.stop()
  }
}
