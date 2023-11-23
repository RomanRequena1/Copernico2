package consumers.registral.exclusiones_sujeto.infrastructure

import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoCommands.ExclusionesSujetoUpdateFromDto
import consumers.registral.exclusiones_sujeto.application.entities.{ExclusionesSujetoAnt, ExclusionesSujetoExternalDto, ExclusionesSujetoTri}
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoResponses.GetExclusionesSujetoResponse
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

package object json {
  //COMMANDS
  implicit val ExclusionesSujetoUpdateFromDtoDecoder: Decoder[ExclusionesSujetoUpdateFromDto] = deriveDecoder
  implicit val ExclusionesSujetoUpdateFromDtoEncoder: Encoder[ExclusionesSujetoUpdateFromDto] = deriveEncoder

  //EXTERNALDTO
  implicit val ExclusionesSujetoTriDecoder: Decoder[ExclusionesSujetoTri] = deriveDecoder
  implicit val ExclusionesSujetoTriEncoder: Encoder[ExclusionesSujetoTri] = deriveEncoder

  implicit val ExclusionesSujetoAntDecoder: Decoder[ExclusionesSujetoAnt] = deriveDecoder
  implicit val ExclusionesSujetoAntEncoder: Encoder[ExclusionesSujetoAnt] = deriveEncoder

  implicit val ExclusionesSujetoDtoDecoder: Decoder[ExclusionesSujetoExternalDto] = deriveDecoder
  implicit val ExclusionesSujetoDtoEncoder: Encoder[ExclusionesSujetoExternalDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }


  //RESPONSES
  implicit val GetExclusionesSujetoResponseDecoder: Decoder[GetExclusionesSujetoResponse] = deriveDecoder
  implicit val GetExclusionesSujetoResponseEncoder: Encoder[GetExclusionesSujetoResponse] = deriveEncoder

  //EVENTS
  implicit val ExclusionesSujetoUpdatedFromDtoDecoder: Decoder[ExclusionesSujetoUpdatedFromDto] = deriveDecoder
  implicit val ExclusionesSujetoUpdatedFromDtoEncoder: Encoder[ExclusionesSujetoUpdatedFromDto] = deriveEncoder
}
