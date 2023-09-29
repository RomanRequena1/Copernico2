package consumers.registral.subasta.infrastructure

import ai.x.play.json.Jsonx
import consumers.registral.subasta.application.entities.SubastaCommands.SubastaUpdateFromDto
import consumers.registral.subasta.application.entities.SubastaExternalDto
import consumers.registral.subasta.application.entities.SubastaResponses.GetSubastaResponse
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import consumers.registral.subasta.domain.{SubastaEvents, SubastaState}
import play.api.libs.json.Json
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import play.api.libs.json.{Format, Json}
object json {

  //DTO
  implicit val SubastaExternalDtoDecoder: Decoder[SubastaExternalDto] = deriveDecoder
  implicit val SubastaExternalDtoEncoder: Encoder[SubastaExternalDto] = deriveEncoder


  //EVENT
  implicit val SubastaUpdatedFromDtoDecoder: Decoder[SubastaUpdatedFromDto] = deriveDecoder
  implicit val SubastaUpdatedFromDtoEncoder: Encoder[SubastaUpdatedFromDto] = deriveEncoder


  //COMMAND

  implicit val SubastaUpdateFromDtoDecoder: Decoder[SubastaUpdateFromDto] = deriveDecoder
  implicit val SubastaUpdateFromDtoEncoder: Encoder[SubastaUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val GetSubastaResponseDecoder: Decoder[GetSubastaResponse] = deriveDecoder
  implicit val GetSubastaResponseEncoder: Encoder[GetSubastaResponse] = deriveEncoder

}
