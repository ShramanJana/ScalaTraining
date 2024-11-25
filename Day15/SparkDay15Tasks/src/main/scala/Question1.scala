import org.apache.spark.{SparkConf, SparkContext}
import scala.util.Random

object Question1 {

  def main(args: Array[String]): Unit = {

    // Initialize SparkSession
    val conf = new SparkConf()
      .setAppName("RDD Partitions")
      .setMaster("local[*]") // Use all available cores
      .set("spark.driver.host","localhost")

    val sc = new SparkContext(conf)
    // Generate 10 million random integers
    val data = Seq.fill(10000000)(Random.nextInt(1000))
    val rdd = sc.parallelize(data)

    // Check the number of partitions
    println(s"Initial number of partitions: ${rdd.getNumPartitions}")

    // Repartition the RDD into 4 partitions
    val repartitionedRdd = rdd.repartition(4)
    println(s"Number of partitions after repartitioning: ${repartitionedRdd.getNumPartitions}")

    // Analyze data distribution
    val repartitionedData = repartitionedRdd.glom().collect()
    for ((partition, index) <- repartitionedData.zipWithIndex) {
      println(s"Partition $index size after repartitioning: ${partition.length}")
    }

    // Coalesce the RDD back into 2 partitions
    val coalescedRdd = repartitionedRdd.coalesce(2)
    println(s"Number of partitions after coalescing: ${coalescedRdd.getNumPartitions}")

    // Print the first 5 elements from each partition
    val coalescedData = coalescedRdd.glom().collect()
    for ((partition, index) <- coalescedData.zipWithIndex) {
      println(s"First 5 elements from partition $index: ${partition.take(5).mkString(", ")}")
    }

    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    sc.stop()
  }

}
