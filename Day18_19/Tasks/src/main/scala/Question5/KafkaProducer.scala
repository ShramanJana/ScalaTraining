package Question5

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import scala.concurrent.ExecutionContext.Implicits.global
import java.util.Properties
import java.util.concurrent.atomic.AtomicBoolean
import scala.concurrent.Future
import scala.util.Random

object KafkaProducer {
  def main(args: Array[String]): Unit = {

    // Kafka producer properties
    val props = new Properties()
    props.put("bootstrap.servers", "localhost:9092")
    props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")

    val producer = new KafkaProducer[String, String](props)

    // Random data generator
    val random = new Random()

    // Function to send a message
    def sendRecord(): Unit = {
      val orderId = random.nextInt(100) + 1          // Random orderId between 1 and 100
      val userId = random.nextInt(1000) + 1          // Random userId between 1 and 1000
      val amount = random.nextDouble() * 1000        // Random transaction amount

      // Create a JSON message
      val message = s"""{"orderId": $orderId, "userId": $userId, "amount": $amount}"""

      // Send the message to Kafka
      val record = new ProducerRecord[String, String]("orders", null, message)
      producer.send(record)

      // Print the message with the timestamp
      println(s"Message sent: $message")
    }

    // Use a scheduler to send a record every 1 second
    println("Starting Kafka producer. Sending messages every 1 second...")
    val isRunning = new AtomicBoolean(true) // Atomic flag to control the loop
    Future {
      while (isRunning.get()) {
        sendRecord()
        Thread.sleep(1000) // Wait for 1 second before sending the next message
      }
    }

    // Add shutdown hook to close the producer gracefully
    sys.addShutdownHook {
      println("Shutting down Kafka producer...")
      isRunning.set(false) // Stop the loop
      producer.close()
    }

    // Block the main thread to keep the program running
    while (isRunning.get()) {
      Thread.sleep(100) // Small sleep to avoid excessive CPU usage
    }
    println("Kafka producer stopped.")

  }
}
