import org.apache.spark.{SparkConf, SparkContext}
import java.io._
import scala.util.Random

object Question5 {

  def main(args: Array[String]): Unit = {
    // Initialize SparkSession
    val conf = new SparkConf()
      .setAppName("Partitioning Impact on Performance")
      .setMaster("local[*]") // Use all available cores
      .set("spark.driver.host","localhost")

    val sc = new SparkContext(conf)

    val filePath = "./large_dataset_2.csv" // Change this to your dataset path
    val rdd = sc.textFile(filePath)

    // Skip the first row (header)
    val data = rdd.mapPartitionsWithIndex((index, iterator) =>
      if (index == 0) iterator.drop(1) else iterator
    )

    // Task: Count the number of rows in the RDD (this is a narrow transformation)
    val rowCount = data.count()
    println(s"Total number of rows: $rowCount")

    // Helper Method for Timing Execution
    def time[R](block: => R): R = {
      val start = System.currentTimeMillis()
      val result = block
      val end = System.currentTimeMillis()
      println(s"Execution Time: ${end - start} ms")
      result
    }

    // Count the Number of Rows
    Seq(2, 4, 8).foreach { numPartitions =>
      println(s"\n--- Partitioning into $numPartitions partitions ---")
      val partitionedData = data.repartition(numPartitions)

      time {
        val count = partitionedData.count()
        println(s"Row count: $count")
      }
    }

    // Write the Output Back to Disk
    Seq(2, 4, 8).foreach { numPartitions =>
      println(s"\n--- Sorting and Writing to disk with $numPartitions partitions ---")
      val partitionedData = data.repartition(numPartitions)

      time {
        val sortedData = partitionedData.sortBy(line => line.split(",")(4).toInt)
        sortedData.saveAsTextFile(s"output/sorted_data_$numPartitions")
      }
    }


    // Hold the Spark UI
    println("Application is running. Press Enter to exit.")
    scala.io.StdIn.readLine()

    sc.stop()
  }
}
