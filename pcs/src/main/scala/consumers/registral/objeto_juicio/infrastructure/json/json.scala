package consumers.registral.objeto_juicio.infrastructure

import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.{ObjetoJuicioUpdateFromDto, RemoveObjetoJuicioFromDto}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.ObjetoJuicioTri
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioResponses.GetObjetoJuicioResponse
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.{ObjetoJuicioRemovedFromDto, ObjetoJuicioUpdatedFromDto}
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

package object json {


  //COMMANDS
  implicit val ObjetoJuicioUpdateFromDtoDecoder: Decoder[ObjetoJuicioUpdateFromDto] = deriveDecoder
  implicit val ObjetoJuicioUpdateFromDtoEncoder: Encoder[ObjetoJuicioUpdateFromDto] = deriveEncoder

  implicit val RemoveObjetoJuicioFromDtoDecoder: Decoder[RemoveObjetoJuicioFromDto] = deriveDecoder
  implicit val RemoveObjetoJuicioFromDtoEncoder: Encoder[RemoveObjetoJuicioFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val ObjetoJuicioTriDecoder: Decoder[ObjetoJuicioTri] = deriveDecoder
  implicit val ObjetoJuicioTriEncoder: Encoder[ObjetoJuicioTri] = deriveEncoder


  implicit val ObjetoJuicioExternalDtoDecoder: Decoder[ObjetoJuicioExternalDto] = deriveDecoder
  implicit val ObjetoJuicioExternalDtoEncoder: Encoder[ObjetoJuicioExternalDto] = deriveEncoder


  //RESPONSES
  implicit val ObjetoJuicioStateDecoder: Decoder[ObjetoJuicioState] = deriveDecoder
  implicit val ObjetoJuicioStateEncoder: Encoder[ObjetoJuicioState] = deriveEncoder

  implicit val GetObjetoJuicioResponseDecoder: Decoder[GetObjetoJuicioResponse] = deriveDecoder
  implicit val GetObjetoJuicioResponseEncoder: Encoder[GetObjetoJuicioResponse] = deriveEncoder

  //EVENTS
  implicit val ObjetoJuicioUpdatedFromDtoDecoder: Decoder[ObjetoJuicioUpdatedFromDto] = deriveDecoder
  implicit val ObjetoJuicioUpdatedFromDtoEncoder: Encoder[ObjetoJuicioUpdatedFromDto] = deriveEncoder

  implicit val ObjetoJuicioRemovedFromDtoDecoder: Decoder[ObjetoJuicioRemovedFromDto] = deriveDecoder
  implicit val ObjetoJuicioRemovedFromDtoEncoder: Encoder[ObjetoJuicioRemovedFromDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
}
