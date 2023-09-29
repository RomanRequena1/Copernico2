package consumers.registral.domicilio_objeto.infrastructure

import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoCommands.DomicilioObjetoUpdateFromDto
import consumers.registral.domicilio_objeto.application.entities.DomicilioObjetoResponses.GetDomicilioObjetoResponse
import consumers.registral.domicilio_objeto.application.entities.{DomicilioObjetoAnt, DomicilioObjetoTri}
import consumers.registral.domicilio_objeto.domain.DomicilioObjetoEvents.DomicilioObjetoUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

package object json {

 //COMMANDS
  implicit val DomicilioObjetoRemoveDecoder: Decoder[DomicilioObjetoUpdateFromDto] = deriveDecoder
  implicit val DomicilioObjetoRemoveEncoder: Encoder[DomicilioObjetoUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val DomicilioObjetoTriDecoder: Decoder[DomicilioObjetoTri] = deriveDecoder
  implicit val DomicilioObjetoTriEncoder: Encoder[DomicilioObjetoTri] = deriveEncoder

  implicit val DomicilioObjetoAntDecoder: Decoder[DomicilioObjetoAnt] = deriveDecoder
  implicit val DomicilioObjetoAntEncoder: Encoder[DomicilioObjetoAnt] = deriveEncoder

  //RESPONSES
  implicit val GetDomicilioObjetoResponseDecoder: Decoder[GetDomicilioObjetoResponse] = deriveDecoder
  implicit val GetDomicilioObjetoResponseEncoder: Encoder[GetDomicilioObjetoResponse] = deriveEncoder

  //EVENTS
  implicit val DomicilioObjetoUpdatedFromDtoDecoder: Decoder[DomicilioObjetoUpdatedFromDto] = deriveDecoder
  implicit val DomicilioObjetoUpdatedFromDtoEncoder: Encoder[DomicilioObjetoUpdatedFromDto] = deriveEncoder


}
