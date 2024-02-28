package consumers.registral.tramite.infrastructure

import consumers.registral.tramite.application.entities.Tramite
import consumers.registral.tramite.application.entities.TramiteCommands.TramiteUpdateFromDto
import consumers.registral.tramite.application.entities.TramiteResponses.GetTramiteResponse
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

package object json {

  //COMMANDS
  implicit val ObligacionUpdateFromDtoDecoder: Decoder[TramiteUpdateFromDto] = deriveDecoder
  implicit val ObligacionUpdateFromDtoEncoder: Encoder[TramiteUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val TramiteDecoder: Decoder[Tramite] = deriveDecoder
  implicit val TramiteEncoder: Encoder[Tramite] = deriveEncoder
  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
  //RESPONSES
  implicit val GetTramiteResponseDecoder: Decoder[GetTramiteResponse] = deriveDecoder
  implicit val GetTramiteResponseEncoder: Encoder[GetTramiteResponse] = deriveEncoder

  //EVENTS
  implicit val TramiteUpdatedFromDtoDecoder: Decoder[TramiteUpdatedFromDto] = deriveDecoder
  implicit val TramiteUpdatedFromDtoEncoder: Encoder[TramiteUpdatedFromDto] = deriveEncoder
}
