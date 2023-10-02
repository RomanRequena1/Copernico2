package consumers.registral.parametrica_plan.infrastructure.json

import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanCommands.ParametricaPlanUpdateFromDto
import consumers.registral.parametrica_plan.application.entities.{ParametricaPlanAnt, ParametricaPlanTri}
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanResponses.GetParametricaPlanResponse
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoCommands.ParametricaRecargoUpdateFromDto
import consumers.registral.parametrica_recargo.application.entities.ParametricaRecargoResponses.GetParametricaRecargoResponse
import consumers.registral.parametrica_recargo.application.entities.{ParametricaRecargoAnt, ParametricaRecargoTri}
import consumers.registral.parametrica_recargo.domain.ParametricaRecargoEvents.ParametricaRecargoUpdatedFromDto
import play.api.libs.json.Json
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}
import io.leonard.TraitFormat
import io.leonard.TraitFormat.traitFormat
import play.api.libs.json.Json

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object ParametricaRecargoImplicits {

  //DTO
  implicit val ParametricaPlanTriTriDecoder: Decoder[ParametricaPlanTri] = deriveDecoder
  implicit val ParametricaPlanTriTriEncoder: Encoder[ParametricaPlanTri] = deriveEncoder

  implicit val ParametricaPlanAntDecoder: Decoder[ParametricaPlanAnt] = deriveDecoder
  implicit val ParametricaPlanAntEncoder: Encoder[ParametricaPlanAnt] = deriveEncoder
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

