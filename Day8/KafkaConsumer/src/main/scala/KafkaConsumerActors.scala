import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord}
import spray.json.enrichAny
import JsonFormats._

import java.util.Properties

class MessageGathererActor(producer: KafkaProducer[String, String]) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println("MessageGatherer received the message")
      val jsonString = msg.toJson.toString()
      val record = new ProducerRecord[String, String]("consolidated-messages", msg.messageKey, msg.toJson.toString())
      producer.send(record)
      println(s"Produced $jsonString to consolidated-message topic")
  }
}

class NetworkKafkaListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println("Network Kafka Listener consumed the message")
      messageGatherer ! msg
  }
}

class AppKafkaListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println("App Kafka Listener consumed the message")
      messageGatherer ! msg
  }
}

class CloudKafkaListener(messageGatherer: ActorRef) extends Actor {
  def receive: Receive = {
    case msg: Message =>
      println("Cloud Kafka Listener consumed the message")
      messageGatherer ! msg
  }
}
object KafkaConsumerActors {
    val props = new Properties()
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sys.env.get("BROKER_HOST").getOrElse("localhost")+":9092")
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer")
    val producer = new KafkaProducer[String, String](props)

    val system = ActorSystem("KafkaConsumerSystem")
    val messageGathererActor = system.actorOf(Props(new MessageGathererActor(producer)), "MessageGatherer")
    val networkKafkaListener = system.actorOf(Props(new NetworkKafkaListener(messageGathererActor)), "NetworkKafkaListener")
    val appKafkaListener = system.actorOf(Props(new AppKafkaListener(messageGathererActor)), "AppKafkaListener")
    val cloudKafkaListener = system.actorOf(Props(new CloudKafkaListener(messageGathererActor)), "CloudKafkaListener")

}
