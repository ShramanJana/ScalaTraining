import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class Message(messageType: String, message: String, messageKey: String)

object JsonFormats {
  implicit val messageFormat: RootJsonFormat[Message] = jsonFormat3(Message)
}