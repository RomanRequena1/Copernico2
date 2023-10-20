package consumers.registral.actividad_sujeto.infrastructure.json

import consumers.registral.actividad_sujeto.application.entities.{ActividadSujeto, DetallesActividadSujeto}
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoCommands.ActividadSujetoUpdateFromDto
import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoResponses.GetActividadSujetoResponse
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try


object json {
  implicit val ActividadSujetoUpdateFromDtoDecoder: Decoder[ActividadSujetoUpdateFromDto] = deriveDecoder
  implicit val ActividadSujetoUpdateFromDtoEncoder: Encoder[ActividadSujetoUpdateFromDto] = deriveEncoder

  implicit val ActividadSujetoDecoder: Decoder[ActividadSujeto] = deriveDecoder
  implicit val ActividadSujetoEncoder: Encoder[ActividadSujeto] = deriveEncoder

  implicit val DetallesActividadSujetoDecoder: Decoder[DetallesActividadSujeto] = deriveDecoder
  implicit val DetallesActividadSujetoEncoder: Encoder[DetallesActividadSujeto] = deriveEncoder


  implicit val GetActividadSujetoResponseDecoder: Decoder[GetActividadSujetoResponse] = deriveDecoder
  implicit val GetActividadSujetoResponseEncoder: Encoder[GetActividadSujetoResponse] = deriveEncoder

  implicit val ActividadSujetoUpdatedFromDtoDecoder: Decoder[ActividadSujetoUpdatedFromDto] = deriveDecoder
  implicit val ActividadSujetoUpdatedFromDtoEncoder: Encoder[ActividadSujetoUpdatedFromDto] = deriveEncoder
  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
}
