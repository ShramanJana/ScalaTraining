import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import JsonFormats._
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.

object AppConstants {
  val NETWORK_MESSAGE = "NetworkMessage"
  val APP_MESSAGE = "AppMessage"
  val CLOUD_MESSAGE = "CloudMessage"
}

object Main {
  implicit val system = ActorSystem(Behaviors.empty, "MyActorSystem")

  def main(args: Array[String]): Unit = {
    val route =
      post {
        path("process-Message") {
          entity(as[Message]) { message =>
            KafkaProducerActors.sendKafkaMessage(message)
            complete(StatusCodes.OK, s"Message sent to Kafka: $message")
          }
        }
    }

    Http().newServerAt("0.0.0.0", 8080).bind(route)
    println("Server online at http://0.0.0.0:8080/")
  }
}
