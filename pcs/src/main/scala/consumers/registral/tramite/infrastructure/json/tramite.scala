package consumers.registral.tramite.infrastructure

import consumers.registral.tramite.application.entities.Tramite
import consumers.registral.tramite.application.entities.TramiteCommands.TramiteUpdateFromDto
import consumers.registral.tramite.application.entities.TramiteResponses.GetTramiteResponse
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object json {

  //COMMANDS
  implicit val ObligacionUpdateFromDtoDecoder: Decoder[TramiteUpdateFromDto] = deriveDecoder
  implicit val ObligacionUpdateFromDtoEncoder: Encoder[TramiteUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val TramiteDecoder: Decoder[Tramite] = deriveDecoder
  implicit val TramiteEncoder: Encoder[Tramite] = deriveEncoder

  //RESPONSES
  implicit val GetTramiteResponseDecoder: Decoder[GetTramiteResponse] = deriveDecoder
  implicit val GetTramiteResponseEncoder: Encoder[GetTramiteResponse] = deriveEncoder

  //EVENTS
  implicit val TramiteUpdatedFromDtoDecoder: Decoder[TramiteUpdatedFromDto] = deriveDecoder
  implicit val TramiteUpdatedFromDtoEncoder: Encoder[TramiteUpdatedFromDto] = deriveEncoder
}
