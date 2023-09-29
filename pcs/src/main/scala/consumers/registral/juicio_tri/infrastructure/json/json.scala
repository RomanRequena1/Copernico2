package consumers.registral.juicio_tri.infrastructure

import consumers.registral.juicio_tri.application.entities.JuicioDosCommands.{JuicioDosRemoveFromDto, JuicioDosUpdateFromDto}
import consumers.registral.juicio_tri.application.entities.{ JuicioDosTri}
import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import consumers.registral.juicio_tri.domain.JuicioDosEvents.{JuicioDosRemovedFromDto, JuicioDosUpdatedFromDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.leonard.TraitFormat
import io.leonard.TraitFormat.traitFormat
import play.api.libs.json.Json

object json {

  //DTO
  implicit val JuicioDosTriDecoder: Decoder[JuicioDosTri] = deriveDecoder
  implicit val JuicioDosTriEncoder: Encoder[JuicioDosTri] = deriveEncoder


  //EVENT
  implicit val JuicioDosUpdatedFromDtoDecoder: Decoder[JuicioDosUpdatedFromDto] = deriveDecoder
  implicit val JuicioDosUpdatedFromDtoEncoder: Encoder[JuicioDosUpdatedFromDto] = deriveEncoder

  implicit val JuicioDosRemovedFromDtoDecoder: Decoder[JuicioDosRemovedFromDto] = deriveDecoder
  implicit val JuicioDosRemovedFromDtoEncoder: Encoder[JuicioDosRemovedFromDto] = deriveEncoder

  //COMMAND

  implicit val JuicioDosUpdateFromDtoDecoder: Decoder[JuicioDosUpdateFromDto] = deriveDecoder
  implicit val JuicioDosUpdateFromDtoEncoder: Encoder[JuicioDosUpdateFromDto] = deriveEncoder

  implicit val JuicioDosRemoveFromDtoDecoder: Decoder[JuicioDosRemoveFromDto] = deriveDecoder
  implicit val JuicioDosRemoveFromDtoEncoder: Encoder[JuicioDosRemoveFromDto] = deriveEncoder

  //REPONDS

  implicit val GetJuicioDosResponseDecoder: Decoder[GetJuicioDosResponse] = deriveDecoder
  implicit val GetJuicioDosResponseEncoder: Encoder[GetJuicioDosResponse] = deriveEncoder

}
