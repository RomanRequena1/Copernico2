package consumers.registral.calendario.infrastructure

import consumers.registral.calendario.application.entities.CalendarioCommands.CalendarioUpdateFromDto
import consumers.registral.calendario.application.entities.CalendarioExternalDto
import consumers.registral.calendario.application.entities.CalendarioResponses.GetCalendarioResponse
import consumers.registral.calendario.domain.CalendarioEvents.CalendarioUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import play.api.libs.json.Json

object json {
  implicit val CalendarioUpdateFromDtoDecoder: Decoder[CalendarioUpdateFromDto] = deriveDecoder
  implicit val CalendarioUpdateFromDtoEncoder: Encoder[CalendarioUpdateFromDto] = deriveEncoder

  implicit val CalendarioDecoder: Decoder[CalendarioExternalDto] = deriveDecoder
  implicit val CalendarioEncoder: Encoder[CalendarioExternalDto] = deriveEncoder

  implicit val GetCalendarioResponseDecoder: Decoder[GetCalendarioResponse] = deriveDecoder
  implicit val GetCalendarioResponseEncoder: Encoder[GetCalendarioResponse] = deriveEncoder

  implicit val CalendarioUpdatedFromDtoDecoder: Decoder[CalendarioUpdatedFromDto] = deriveDecoder
  implicit val CalendarioUpdatedFromDtoEncoder: Encoder[CalendarioUpdatedFromDto] = deriveEncoder
}
