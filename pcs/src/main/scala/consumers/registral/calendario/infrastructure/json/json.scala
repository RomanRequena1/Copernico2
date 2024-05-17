package consumers.registral.calendario.infrastructure.json

import consumers.registral.calendario.application.entities.CalendarioCommands.CalendarioUpdateFromDto
import consumers.registral.calendario.application.entities.CalendarioExternalDto
import consumers.registral.calendario.application.entities.CalendarioResponses.GetCalendarioResponse
import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object json {
  implicit val CalendarioUpdateFromDtoDecoder: Decoder[CalendarioUpdateFromDto] = deriveDecoder
  implicit val CalendarioUpdateFromDtoEncoder: Encoder[CalendarioUpdateFromDto] = deriveEncoder

  implicit val CalendarioDecoder: Decoder[CalendarioExternalDto] = deriveDecoder
  implicit val CalendarioEncoder: Encoder[CalendarioExternalDto] = deriveEncoder

  implicit val GetCalendarioResponseDecoder: Decoder[GetCalendarioResponse] = deriveDecoder
  implicit val GetCalendarioResponseEncoder: Encoder[GetCalendarioResponse] = deriveEncoder

  implicit val CalendarioUpdatedFromDtoDecoder: Decoder[CalendarioUpdatedFromDto] = deriveDecoder
  implicit val CalendarioUpdatedFromDtoEncoder: Encoder[CalendarioUpdatedFromDto] = deriveEncoder
  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
}
