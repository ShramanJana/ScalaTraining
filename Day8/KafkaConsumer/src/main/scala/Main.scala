import AppConstants._
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.kafka.scaladsl.Consumer
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.stream.scaladsl.Sink
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import JsonFormats._
import KafkaConsumerActors._
import spray.json._

object AppConstants {
  val NETWORK_MESSAGE = "network-message"
  val APP_MESSAGE = "app-message"
  val CLOUD_MESSAGE = "cloud-message"
}

object Main {

  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[_] = ActorSystem(Behaviors.empty, "kafkaConsumerSystem")
      val consumerSettings = ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(sys.env.get("BROKER_HOST").getOrElse("localhost")+":9092")
      .withGroupId("group1")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest")

    val topics = Set(NETWORK_MESSAGE, APP_MESSAGE, CLOUD_MESSAGE)

    Consumer.plainSource(consumerSettings, Subscriptions.topics(topics))
      .map(record => {
        val msg = record.value().parseJson.convertTo[Message]
        record.topic() match {
          case NETWORK_MESSAGE => networkKafkaListener ! msg
          case APP_MESSAGE => appKafkaListener ! msg
          case CLOUD_MESSAGE => cloudKafkaListener ! msg
        }
      }) // Convert JSON string to Person
      .runWith(Sink.ignore)
  }
}