package consumers.registral.parametrica_plan.infrastructure.json

import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanCommands.ParametricaPlanUpdateFromDto
import consumers.registral.parametrica_plan.application.entities.ParametricaPlanTri
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

object ParametricaRecargoImplicits {

  //DTO
  implicit val ParametricaRecargoTriDecoder: Decoder[ParametricaRecargoTri] = deriveDecoder
  implicit val ParametricaRecargoTriEncoder: Encoder[ParametricaRecargoTri] = deriveEncoder

  implicit val ParametricaRecargoAntDecoder: Decoder[ParametricaRecargoAnt] = deriveDecoder
  implicit val ParametricaRecargoAntEncoder: Encoder[ParametricaRecargoAnt] = deriveEncoder
  //EVENT
  implicit val ParametricaRecargoUpdatedFromDtoDecoder: Decoder[ParametricaRecargoUpdatedFromDto] = deriveDecoder
  implicit val ParametricaRecargoUpdatedFromDtoEncoder: Encoder[ParametricaRecargoUpdatedFromDto] = deriveEncoder


  //COMMAND

  implicit val ParametricaRecargoUpdateFromDtoDecoder: Decoder[ParametricaRecargoUpdateFromDto] = deriveDecoder
  implicit val ParametricaRecargoUpdateFromDtoEncoder: Encoder[ParametricaRecargoUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val GetParametricaRecargoResponseDecoder: Decoder[GetParametricaRecargoResponse] = deriveDecoder
  implicit val GetParametricaRecargoResponseEncoder: Encoder[GetParametricaRecargoResponse] = deriveEncoder

}

