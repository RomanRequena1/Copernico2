package consumers.registral.parametrica_recargo.infrastructure.json

import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoExternalDto.{ParametricaRecargoAnt, ParametricaRecargoTri}
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoResponses.GetParametricaRecargoResponse
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object json {

  //DTO
  implicit val ParametricaRecargoTriDecoder: Decoder[ParametricaRecargoTri] = deriveDecoder
  implicit val ParametricaRecargoTriEncoder: Encoder[ParametricaRecargoTri] = deriveEncoder

  implicit val ParametricaRecargoAntDecoder: Decoder[ParametricaRecargoAnt] = deriveDecoder
  implicit val ParametricaRecargoAntEncoder: Encoder[ParametricaRecargoAnt] = deriveEncoder

  implicit val ParametricaParametricaRecargoExternalDtoDecoder: Decoder[ParametricaRecargoExternalDto] = deriveDecoder
  implicit val ParametricaRecargoExternalDtoEncoder: Encoder[ParametricaRecargoExternalDto] = deriveEncoder
  //EVENT
  implicit val ParametricaRecargoUpdatedFromDtoDecoder: Decoder[ParametricaRecargoUpdatedFromDto] = deriveDecoder
  implicit val ParametricaRecargoUpdatedFromDtoEncoder: Encoder[ParametricaRecargoUpdatedFromDto] = deriveEncoder
  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }

  //COMMAND

  implicit val ParametricaRecargoUpdateFromDtoDecoder: Decoder[ParametricaRecargoUpdateFromDto] = deriveDecoder
  implicit val ParametricaRecargoUpdateFromDtoEncoder: Encoder[ParametricaRecargoUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val GetParametricaRecargoResponseDecoder: Decoder[GetParametricaRecargoResponse] = deriveDecoder
  implicit val GetParametricaRecargoResponseEncoder: Encoder[GetParametricaRecargoResponse] = deriveEncoder

}
