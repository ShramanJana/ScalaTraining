import akka.actor.ActorSystem
import consumers.{BookingConfirmationConsumer, RoomPreparationConsumer}

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("NotificationSystem")

  // Start Kafka consumers
  BookingConfirmationConsumer.startConsumer()
  RoomPreparationConsumer.startConsumer()

  // Keep the application running
  println("Notification System is running. Press ENTER to stop.")
  scala.io.StdIn.readLine() // Waits for ENTER to terminate
  system.terminate()         // Gracefully shut down the actor system
}
