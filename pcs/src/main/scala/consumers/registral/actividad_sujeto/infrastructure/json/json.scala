package consumers.registral.actividad_sujeto.infrastructure

import consumers.registral.actividad_sujeto.application.entities.ActividadSujeto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoResponses.GetActividadSujetoResponse
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}


object json {
  implicit val ActividadSujetoUpdateFromDtoDecoder: Decoder[ActividadSujetoUpdateFromDto] = deriveDecoder
  implicit val ActividadSujetoUpdateFromDtoEncoder: Encoder[ActividadSujetoUpdateFromDto] = deriveEncoder

  implicit val ActividadSujetoDecoder: Decoder[ActividadSujeto] = deriveDecoder
  implicit val ActividadSujetoEncoder: Encoder[ActividadSujeto] = deriveEncoder

  implicit val GetActividadSujetoResponseDecoder: Decoder[GetActividadSujetoResponse] = deriveDecoder
  implicit val GetActividadSujetoResponseEncoder: Encoder[GetActividadSujetoResponse] = deriveEncoder

  implicit val ActividadSujetoUpdatedFromDtoDecoder: Decoder[ActividadSujetoUpdatedFromDto] = deriveDecoder
  implicit val ActividadSujetoUpdatedFromDtoEncoder: Encoder[ActividadSujetoUpdatedFromDto] = deriveEncoder
}
