package consumers.registral.objeto_juicio.infrastructure

import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.ObjetoJuicioUpdateFromDto
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto.{ObjetoJuicioAnt, ObjetoJuicioTri}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioResponses.GetObjetoJuicioResponse
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

package object json {


  //COMMANDS
  implicit val ObjetoJuicioUpdateFromDtoDecoder: Decoder[ObjetoJuicioUpdateFromDto] = deriveDecoder
  implicit val ObjetoJuicioUpdateFromDtoEncoder: Encoder[ObjetoJuicioUpdateFromDto] = deriveEncoder


  //EXTERNALDTO
  implicit val ObjetoJuicioTriDecoder: Decoder[ObjetoJuicioTri] = deriveDecoder
  implicit val ObjetoJuicioTriEncoder: Encoder[ObjetoJuicioTri] = deriveEncoder

  implicit val ObjetoJuicioAntDecoder: Decoder[ObjetoJuicioAnt] = deriveDecoder
  implicit val ObjetoJuicioAntEncoder: Encoder[ObjetoJuicioAnt] = deriveEncoder

  implicit val ObjetoJuicioExternalDtoDecoder: Decoder[ObjetoJuicioExternalDto] = deriveDecoder
  implicit val ObjetoJuicioExternalDtoEncoder: Encoder[ObjetoJuicioExternalDto] = deriveEncoder


  //RESPONSES
  implicit val GetObjetoJuicioResponseDecoder: Decoder[GetObjetoJuicioResponse] = deriveDecoder
  implicit val GetObjetoJuicioResponseEncoder: Encoder[GetObjetoJuicioResponse] = deriveEncoder

  //EVENTS
  implicit val ObjetoJuicioUpdatedFromDtoDecoder: Decoder[ObjetoJuicioUpdatedFromDto] = deriveDecoder
  implicit val ObjetoJuicioUpdatedFromDtoEncoder: Encoder[ObjetoJuicioUpdatedFromDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
}
