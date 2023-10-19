package consumers.registral.parametrica_plan.infrastructure.json

import consumers.registral.parametrica_plan.application.entities.ParametricaPlanCommands.ParametricaPlanUpdateFromDto
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanExternalDto.{ParametricaPlanAnt, ParametricaPlanTri}
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanResponses.GetParametricaPlanResponse
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object ParametricaPlanImplicits {

  //DTO
  implicit val ParametricaPlanTriDecoder: Decoder[ParametricaPlanTri] = deriveDecoder
  implicit val ParametricaPlanTriEncoder: Encoder[ParametricaPlanTri] = deriveEncoder

  implicit val ParametricaPlanAntDecoder: Decoder[ParametricaPlanAnt] = deriveDecoder
  implicit val ParametricaPlanAntEncoder: Encoder[ParametricaPlanAnt] = deriveEncoder

  implicit val ParametricaPlanExternalDtoDecoder: Decoder[ParametricaPlanExternalDto] = deriveDecoder
  implicit val ParametricaPlanExternalDtoEncoder: Encoder[ParametricaPlanExternalDto] = deriveEncoder
  //EVENT
  implicit val ParametricaPlanUpdateFromDtoDecoder: Decoder[ParametricaPlanUpdateFromDto] = deriveDecoder
  implicit val ParametricaPlanUpdateFromDtoEncoder: Encoder[ParametricaPlanUpdateFromDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
  //COMMAND

  implicit val ParametricaPlanUpdatedFromDtoDecoder: Decoder[ParametricaPlanUpdatedFromDto] = deriveDecoder
  implicit val ParametricaPlanUpdatedFromDtoEncoder: Encoder[ParametricaPlanUpdatedFromDto] = deriveEncoder


  //REPONDS

  implicit val GetParametricaPlanResponseDecoder: Decoder[GetParametricaPlanResponse] = deriveDecoder
  implicit val GetParametricaPlanResponseEncoder: Encoder[GetParametricaPlanResponse] = deriveEncoder

}

