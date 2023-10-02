package consumers.registral.declaracion_jurada.infrastructure.json

import consumers.registral.declaracion_jurada.application.entities.{DeclaracionJurada, DetalleDeclaracionJurada}
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaCommands.DeclaracionJuradaUpdateFromDto
import consumers.registral.declaracion_jurada.application.entities.DeclaracionJuradaResponses.GetDeclaracionJuradaResponse
import consumers.registral.declaracion_jurada.domain.DeclaracionJuradaEvents.DeclaracionJuradaUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder, Json}


object json {
  implicit val DeclaracionJuradaUpdateFromDtoDecoder: Decoder[DeclaracionJuradaUpdateFromDto] = deriveDecoder
  implicit val DeclaracionJuradaUpdateFromDtoEncoder: Encoder[DeclaracionJuradaUpdateFromDto] = deriveEncoder

  implicit val ListDetallesComponenteIDecoder: Decoder[DetalleDeclaracionJurada] = deriveDecoder
  implicit val ListDetallesComponenteIEncoder: Encoder[DetalleDeclaracionJurada] = deriveEncoder

  implicit val DeclaracionJuradaTriDecoder: Decoder[DeclaracionJurada] = deriveDecoder
  implicit val DeclaracionJuradaTriEncoder: Encoder[DeclaracionJurada] = deriveEncoder

  implicit val GetDeclaracionJuradaResponseDecoder: Decoder[GetDeclaracionJuradaResponse] = deriveDecoder
  implicit val GetDeclaracionJuradaResponseEncoder: Encoder[GetDeclaracionJuradaResponse] = deriveEncoder

  implicit val DeclaracionJuradaUpdatedFromDtoDecoder: Decoder[DeclaracionJuradaUpdatedFromDto] = deriveDecoder
  implicit val DeclaracionJuradaUpdatedFromDtoEncoder: Encoder[DeclaracionJuradaUpdatedFromDto] = deriveEncoder

}
