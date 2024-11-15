package utils

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import models.*

object JsonFormats {
  implicit val reservationEncoder: Encoder[Reservation] = deriveEncoder[Reservation]
  implicit val reservationDecoder: Decoder[Reservation] = deriveDecoder[Reservation]

  implicit val roomEncoder: Encoder[Room] = deriveEncoder[Room]
  implicit val roomDecoder: Decoder[Room] = deriveDecoder[Room]

  implicit val userEncoder: Encoder[User] = deriveEncoder[User]
  implicit val userDecoder: Decoder[User] = deriveDecoder[User]
}
