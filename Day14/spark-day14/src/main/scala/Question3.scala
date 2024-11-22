import org.apache.spark.{SparkConf, SparkContext}

object Question3 {
  def main(args: Array[String]): Unit = {
    val conf = new SparkConf().setAppName("Even Numbers").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val rdd = sc.parallelize(Seq(34, 76, 87, 95, 51, 36, 34, 25))

    rdd.filter(_%2 == 0).collect().foreach(println)
    sc.stop()
  }
}
