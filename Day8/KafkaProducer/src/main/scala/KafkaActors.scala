import akka.actor.{Actor, ActorRef, Props, ActorSystem}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import spray.json.enrichAny
import KafkaJsonFormats._

class kafkaMessageActorFactory(networkMessageActor: ActorRef, appMessageActor: ActorRef, cloudMessageActor: ActorRef) extends Actor {
  def receive: Receive = {
    case Message(messageType, message, messageKey) =>
      messageType match {
        case AppConstants.NETWORK_MESSAGE =>
          println(s"Message is of type ${AppConstants.NETWORK_MESSAGE}")
          networkMessageActor ! KafkaMessage(message, messageKey)

        case AppConstants.APP_MESSAGE =>
          println(s"Message is of type ${AppConstants.APP_MESSAGE}")
          appMessageActor ! KafkaMessage(message, messageKey)

        case AppConstants.CLOUD_MESSAGE =>
          println(s"Message is of type ${AppConstants.CLOUD_MESSAGE}")
          cloudMessageActor ! KafkaMessage(message, messageKey)
      }
  }
}

class NetworkMessageActor(producer: KafkaProducer[String, String]) extends Actor {
  val topicName = "network-message"
  def receive: Receive = {
    case msg: KafkaMessage =>
      println(s"Sending kafka message to $topicName topic")
      val jsonString = msg.toJson.toString()
      val record = new ProducerRecord[String, String](topicName, jsonString)
      producer.send(record)
      println(s"Sent to Kafka: $jsonString")
  }
}

class AppMessageActor(producer: KafkaProducer[String, String]) extends Actor {
  val topicName = "app-message"
  def receive: Receive = {
    case msg: KafkaMessage =>
      println(s"Sending kafka message to $topicName topic")
      val jsonString = msg.toJson.toString()
      val record = new ProducerRecord[String, String](topicName, jsonString)
      producer.send(record)
      println(s"Sent to Kafka: $jsonString")
  }
}

class CloudMessageActor(producer: KafkaProducer[String, String]) extends Actor {
  val topicName = "cloud-message"
  def receive: Receive = {
    case msg: KafkaMessage =>
      println(s"Sending kafka message to $topicName topic")
      val jsonString = msg.toJson.toString()
      val record = new ProducerRecord[String, String](topicName, jsonString)
      producer.send(record)
      println(s"Sent to Kafka: $jsonString")
  }
}

object KafkaProducerActors {

  implicit val system = ActorSystem("KafkaProducerSystem")

  val producer = KafkaProducerFactory.createProducer()

  val networkMessageActor = system.actorOf(Props(new NetworkMessageActor(producer)), "NetworkMessageActor")
  val appMessageActor = system.actorOf(Props(new AppMessageActor(producer)), "AppMessageActor")
  val cloudMessageActor = system.actorOf(Props(new CloudMessageActor(producer)), "CloudMessageActor")

  val kafkaMessageActorFactory = system.actorOf(Props(new kafkaMessageActorFactory(networkMessageActor, appMessageActor, cloudMessageActor)))

  def sendKafkaMessage(kafkaMessage: Message) = {
    kafkaMessageActorFactory ! kafkaMessage
  }
}
