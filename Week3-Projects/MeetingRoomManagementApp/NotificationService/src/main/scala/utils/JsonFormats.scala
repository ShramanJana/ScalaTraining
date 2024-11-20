package utils

import io.circe.generic.semiauto.*
import io.circe.{Decoder, Encoder}
import models.*

object JsonFormats {
  implicit val reservationEncoder: Encoder[Reservation] = deriveEncoder[Reservation]
  implicit val reservationDecoder: Decoder[Reservation] = deriveDecoder[Reservation]
}
