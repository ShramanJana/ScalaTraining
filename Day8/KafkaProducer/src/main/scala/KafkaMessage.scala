import spray.json.DefaultJsonProtocol._
import spray.json.RootJsonFormat

case class KafkaMessage(message: String, messageKey: String)

object KafkaJsonFormats {
  implicit val messageFormat: RootJsonFormat[KafkaMessage] = jsonFormat2(KafkaMessage)
}
