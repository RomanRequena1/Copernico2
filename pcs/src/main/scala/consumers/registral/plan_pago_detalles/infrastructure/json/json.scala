package consumers.registral.plan_pago_detalles.infrastructure.json

import consumers.registral.plan_pago_detalles.application.entities.PlanPagoCommands.PlanPagoUpdateFromDto
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoExternalDto
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoExternalDto.{PlanPagoAnt, PlanPagoTri}
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoResponses.GetPlanPagoResponse
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.{PlanPagoRemovedFromDto, PlanPagoUpdatedFromDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object json {

  //DTO
  implicit val PlanPagoExternalDtoDecoder: Decoder[PlanPagoExternalDto] = deriveDecoder
  implicit val PlanPagoExternalDtoEncoder: Encoder[PlanPagoExternalDto] = deriveEncoder

  implicit val PlanPagoRemoveDecoder: Decoder[PlanPagoRemovedFromDto] = deriveDecoder
  implicit val PlanPagoRemoveEncoder: Encoder[PlanPagoRemovedFromDto] = deriveEncoder

  implicit val PlanPagoTriDecoder: Decoder[PlanPagoTri] = deriveDecoder
  implicit val PlanPagoTriEncoder: Encoder[PlanPagoTri] = deriveEncoder

  implicit val PlanPagoAntDecoder: Decoder[PlanPagoAnt] = deriveDecoder
  implicit val PlanPagoAntEncoder: Encoder[PlanPagoAnt] = deriveEncoder

  //implicit val DetallePlanPagoDecoder: Decoder[DetallePlanPago] = deriveDecoder
  //implicit val DetallePlanPagoEncoder: Encoder[DetallePlanPago] = deriveEncoder

  //EVENT
  implicit val PlanPagoUpdatedFromDtoDecoder: Decoder[PlanPagoUpdatedFromDto] = deriveDecoder
  implicit val PlanPagoUpdatedFromDtoEncoder: Encoder[PlanPagoUpdatedFromDto] = deriveEncoder

  implicit val localDateTimeDecoder: Decoder[LocalDateTime] = Decoder.decodeString.emapTry { str =>
    Try(LocalDateTime.parse(str, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S")))
  }
  implicit val localDateTimeEncoder: Encoder[LocalDateTime] = Encoder.encodeString.contramap { dateTime =>
    dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S"))
  }
  //COMMAND
  //implicit val ListDetallesObligacionesDecoder: Decoder[ListaDetallePlanPlago] = deriveDecoder
  //implicit val ListDetallesObligacionesEncoder: Encoder[ListDetallesObligaciones] = deriveEncoder
  //implicit val ListDetallesObligacionesDecoder: Decoder[ListDetallesObligaciones] = deriveDecoder


  /*implicit val ListDetallesObligacionesEncoder: Encoder[ListaDetallePlanPlago] =
    (detallesObligaciones: ListaDetallePlanPlago) =>
      Json.obj(
        "BPL_DETALLES" -> detallesObligaciones.BPL_DETALLES.asJson
      )*/
  implicit val PlanPagoUpdateFromDtoDecoder: Decoder[PlanPagoUpdateFromDto] = deriveDecoder
  implicit val PlanPagoUpdateFromDtoEncoder: Encoder[PlanPagoUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val PlanPagoResponsesDecoder: Decoder[GetPlanPagoResponse] = deriveDecoder
  implicit val PlanPagoResponsesEncoder: Encoder[GetPlanPagoResponse] = deriveEncoder

}
