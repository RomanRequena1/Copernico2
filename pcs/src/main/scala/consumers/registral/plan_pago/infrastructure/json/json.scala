package consumers.registral.plan_pago.infrastructure

import java.time.LocalDateTime
import consumers.registral.plan_pago.application.entities.PlanPagoCommands.PlanPagoUpdateFromDto
import consumers.registral.plan_pago.application.entities.{PlanPagoAnt, PlanPagoResponses, PlanPagoTri}
import consumers.registral.plan_pago.application.entities.PlanPagoResponses.GetPlanPagoResponse
import consumers.registral.plan_pago.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import play.api.libs.json.{Format, Json}
object json {

  //DTO
  implicit val PlanPagoTriDecoder: Decoder[PlanPagoTri] = deriveDecoder
  implicit val PlanPagoTriEncoder: Encoder[PlanPagoTri] = deriveEncoder

  implicit val PlanPagoAntDecoder: Decoder[PlanPagoAnt] = deriveDecoder
  implicit val PlanPagoAntEncoder: Encoder[PlanPagoAnt] = deriveEncoder
  //EVENT
  implicit val PlanPagoUpdatedFromDtoDecoder: Decoder[PlanPagoUpdatedFromDto] = deriveDecoder
  implicit val PlanPagoUpdatedFromDtoEncoder: Encoder[PlanPagoUpdatedFromDto] = deriveEncoder


  //COMMAND

  implicit val PlanPagoUpdateFromDtoDecoder: Decoder[PlanPagoUpdateFromDto] = deriveDecoder
  implicit val PlanPagoUpdateFromDtoEncoder: Encoder[PlanPagoUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val PlanPagoResponsesDecoder: Decoder[PlanPagoResponses] = deriveDecoder
  implicit val PlanPagoResponsesEncoder: Encoder[PlanPagoResponses] = deriveEncoder

}
