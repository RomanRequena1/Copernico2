package consumers.registral.domicilio_sujeto.infrastructure

import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoCommands.DomicilioSujetoUpdateFromDto
import consumers.registral.domicilio_sujeto.application.entities.DomicilioSujetoResponses.GetDomicilioSujetoResponse
import consumers.registral.domicilio_sujeto.application.entities.{DomicilioSujetoAnt, DomicilioSujetoTri}
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object json {

  //COMMANDS
  implicit val DomicilioSujetoRemoveDecoder: Decoder[DomicilioSujetoUpdateFromDto] = deriveDecoder
  implicit val DomicilioSujetoRemoveEncoder: Encoder[DomicilioSujetoUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val DomicilioSujetoTriDecoder: Decoder[DomicilioSujetoTri] = deriveDecoder
  implicit val DomicilioSujetoTriEncoder: Encoder[DomicilioSujetoTri] = deriveEncoder

  implicit val DomicilioSujetoAntDecoder: Decoder[DomicilioSujetoAnt] = deriveDecoder
  implicit val DomicilioSujetoAntEncoder: Encoder[DomicilioSujetoAnt] = deriveEncoder

  //RESPONSES
  implicit val GetDomicilioSujetoResponseDecoder: Decoder[GetDomicilioSujetoResponse] = deriveDecoder
  implicit val getDomicilioSujetoResponseEncoder: Encoder[GetDomicilioSujetoResponse] = deriveEncoder

  //EVENTS
  implicit val DomicilioSujetoUpdatedFromDtoDecoder: Decoder[DomicilioSujetoUpdatedFromDto] = deriveDecoder
  implicit val DomicilioSujetoUpdatedFromDtoEncoder: Encoder[DomicilioSujetoUpdatedFromDto] = deriveEncoder

}
