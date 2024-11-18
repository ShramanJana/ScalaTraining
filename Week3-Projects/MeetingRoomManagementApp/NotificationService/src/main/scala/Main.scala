import actors.{BookingConfirmationActor, RoomPreparationActor, SchedulerActor}
import akka.actor.{ActorSystem, Props}
import consumers.{BookingConfirmationConsumer, RoomPreparationConsumer}
import services.EmailService

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("NotificationSystem")

  // Initialize actors with required parameters
  val roomPreparationActor = system.actorOf(Props(new RoomPreparationActor), "roomPreparationActor")
  val schedulerActor = system.actorOf(SchedulerActor.props(system), "schedulerActor")

  // Start Kafka consumers
  BookingConfirmationConsumer.startConsumer()
  RoomPreparationConsumer.startConsumer()

  // Keep the application running
  println("Notification System is running. Press ENTER to stop.")
  scala.io.StdIn.readLine() // Waits for ENTER to terminate
  system.terminate()         // Gracefully shut down the actor system
}
