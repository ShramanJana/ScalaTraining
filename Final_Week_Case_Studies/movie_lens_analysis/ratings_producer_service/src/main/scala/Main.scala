import akka.actor.ActorSystem
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.SendProducer
import akka.stream.scaladsl.Source
import spray.json._
import RatingJsonProtocol.ratingFormat
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.StringSerializer

import scala.concurrent.duration._
import scala.util.Random

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem = ActorSystem("RatingProducerSystem")
    import system.dispatcher

    // Kafka topic name
    val topic = "movie-ratings"

    // Kafka producer settings
    val producerSettings = ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers("localhost:9092")

    // Kafka producer
    val producer = SendProducer(producerSettings)

    // Stream to generate and publish random movie ratings every 50 ms
    val ratingSource = Source.tick(0.millis, 50.millis, ())
      .map(_ => generateRandomRating())
      .map { rating =>
        val ratingJson = rating.toJson.compactPrint
        new ProducerRecord[String, String](topic, ratingJson)
      }

    // Run the stream
    val done = ratingSource.runForeach(record => producer.send(record))

    // Handle stream completion
    done.onComplete { result =>
      result.fold(
        ex => {
          println(s"Stream failed with error: ${ex.getMessage}")
          system.terminate()
        },
        _ => {
          println("Stream completed successfully")
          system.terminate()
        }
      )
    }

  }

  // Function to generate a random movie rating
  def generateRandomRating(): Rating = {
    val userId = Random.nextInt(1000) + 1
    val movieId = Random.nextInt(1000) + 1
    val rating = Random.nextDouble() * 4.5 + 0.5 // Rating between 0.5 and 5.0
    val timestamp = System.currentTimeMillis()
    Rating(userId, movieId, BigDecimal(rating).setScale(1, BigDecimal.RoundingMode.HALF_UP).toDouble, timestamp)
  }
  
}
