package consumers.registral.juicio_obn.infrastructure.json

import ai.x.play.json.Jsonx
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemove
import consumers.registral.juicio_obn.application.entities.JuicioObnCommands.{JuicioObnDeleteFromDto, JuicioObnUpdateFromDto}
import consumers.registral.juicio_obn.application.entities.{DetallesJuicioTri, JuicioObnTri, ListDetallesJuicioTri}
import consumers.registral.juicio_obn.application.entities.JuicioObnResponses.GetJuicioObnResponses
import consumers.registral.juicio_obn.domain.JuicioObnEvents.{JuicioObnDeletedFromDto, JuicioObnUpdatedFromDto}
import io.circe.{Decoder, Encoder, Json}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps



object json {

  //DTO
  implicit val JuicioObnTriDecoder: Decoder[JuicioObnTri] = deriveDecoder
  implicit val JuicioObnTriEncoder: Encoder[JuicioObnTri] = deriveEncoder


  implicit val DetallesJuicioTriDecoder: Decoder[DetallesJuicioTri] = deriveDecoder
  implicit val DetallesJuicioTriEncoder: Encoder[DetallesJuicioTri] = deriveEncoder

//EVENT
  implicit val JuicioObnUpdatedFromDtoDecoder: Decoder[JuicioObnUpdatedFromDto] = deriveDecoder
  implicit val JuicioObnUpdatedFromDtoEncoder: Encoder[JuicioObnUpdatedFromDto] = deriveEncoder

  implicit val JuicioObnDeletedFromDtoDecoder: Decoder[JuicioObnDeletedFromDto] = deriveDecoder
  implicit val JuicioObnDeletedFromDtoEncoder: Encoder[JuicioObnDeletedFromDto] = deriveEncoder

//COMMAND

  implicit val JuicioObnUpdateFromDtoDecoder: Decoder[JuicioObnUpdateFromDto] = deriveDecoder
  implicit val JuicioObnUpdateFromDtoEncoder: Encoder[JuicioObnUpdateFromDto] = deriveEncoder

  implicit val JuicioObnDeleteFromDtoDecoder: Decoder[JuicioObnDeleteFromDto] = deriveDecoder
  implicit val JuicioObnDeleteFromDtoEncoder: Encoder[JuicioObnDeleteFromDto] = deriveEncoder
  implicit val ListDetallesComponenteIDecoder: Decoder[ListDetallesJuicioTri] = deriveDecoder


  implicit val ListDetallesComponenteIEncoder: Encoder[ListDetallesJuicioTri] =
    (detallesObligaciones: ListDetallesJuicioTri) =>
      Json.obj(
        "BJD_DETALLES" -> detallesObligaciones.BJD_DETALLES.asJson
      )
  //REPONDS

  implicit val GetJuicioObnResponsesDecoder: Decoder[GetJuicioObnResponses] = deriveDecoder
  implicit val GetJuicioObnResponsesEncoder: Encoder[GetJuicioObnResponses] = deriveEncoder


}
