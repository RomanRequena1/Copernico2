package consumers.registral.juicio.infrastructure

import consumers.registral.juicio.application.entities.JuicioCommands.JuicioUpdateFromDto
import consumers.registral.juicio.application.entities.JuicioExternalDto
import consumers.registral.juicio.application.entities.JuicioExternalDto.{DetallesJuicio, JuicioAnt, JuicioTri, ListDetallesJuicio}
import consumers.registral.juicio.application.entities.JuicioResponses.GetJuicioResponse
import consumers.registral.juicio.domain.JuicioEvents.JuicioUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

package object json {


  //COMMANDS
  implicit val JuicioUpdateFromDtoDecoder: Decoder[JuicioUpdateFromDto] = deriveDecoder
  implicit val JuicioUpdateFromDtoEncoder: Encoder[JuicioUpdateFromDto] = deriveEncoder
  implicit val ListDetallesComponenteIDecoder: Decoder[ListDetallesJuicio] = deriveDecoder


  implicit val ListDetallesComponenteIEncoder: Encoder[ListDetallesJuicio] =
    (detallesObligaciones: ListDetallesJuicio) =>
      Json.obj(
        "BJU_DETALLES" -> detallesObligaciones.BJU_DETALLES.asJson
      )
  //EXTERNALDTO
  implicit val JuicioTriDecoder: Decoder[JuicioTri] = deriveDecoder
  implicit val JuicioTriEncoder: Encoder[JuicioTri] = deriveEncoder

  implicit val JuicioAntDecoder: Decoder[JuicioAnt] = deriveDecoder
  implicit val JuicioAntEncoder: Encoder[JuicioAnt] = deriveEncoder

  implicit val DetallesJuicioDecoder: Decoder[DetallesJuicio] = deriveDecoder
  implicit val DetallesJuicioEncoder: Encoder[DetallesJuicio] = deriveEncoder

  implicit val JuicioExternalDtoDecoder: Decoder[JuicioExternalDto] = deriveDecoder
  implicit val JuicioExternalDtoEncoder: Encoder[JuicioExternalDto] = deriveEncoder


  //RESPONSES
  implicit val GetJuicioResponseDecoder: Decoder[GetJuicioResponse] = deriveDecoder
  implicit val GetJuicioResponseEncoder: Encoder[GetJuicioResponse] = deriveEncoder

  //EVENTS
  implicit val JuicioUpdatedFromDtoDecoder: Decoder[JuicioUpdatedFromDto] = deriveDecoder
  implicit val JuicioUpdatedFromDtoEncoder: Encoder[JuicioUpdatedFromDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
}
