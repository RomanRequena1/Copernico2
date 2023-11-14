package consumers.registral.plan_cabecera.infrastructure.json

import consumers.registral.plan_cabecera.application.entities.PlanCabeceraCommands.PlanCabeceraUpdateFromDto
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraExternalDto
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraExternalDto.{PlanCabeceraAnt, PlanCabeceraTri}
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraResponses.GetPlanCabeceraResponse
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.{PlanCabeceraRemovedFromDto, PlanCabeceraUpdatedFromDto}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.circe.{Decoder, Encoder}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.util.Try

object json {
  //DTO
  implicit val PlanCabeceraExternalDtoDecoder: Decoder[PlanCabeceraExternalDto] = deriveDecoder
  implicit val PlanCabeceraExternalDtoEncoder: Encoder[PlanCabeceraExternalDto] = deriveEncoder

  implicit val PlanCabeceraRemoveDecoder: Decoder[PlanCabeceraRemovedFromDto] = deriveDecoder
  implicit val PlanCabeceraRemoveEncoder: Encoder[PlanCabeceraRemovedFromDto] = deriveEncoder

  implicit val PlanCabeceraTriDecoder: Decoder[PlanCabeceraTri] = deriveDecoder
  implicit val PlanCabeceraTriEncoder: Encoder[PlanCabeceraTri] = deriveEncoder

  implicit val PlanCabeceraAntDecoder: Decoder[PlanCabeceraAnt] = deriveDecoder
  implicit val PlanCabeceraAntEncoder: Encoder[PlanCabeceraAnt] = deriveEncoder

  //implicit val DetallePlanPagoDecoder: Decoder[DetallePlanPago] = deriveDecoder
  //implicit val DetallePlanPagoEncoder: Encoder[DetallePlanPago] = deriveEncoder

  //EVENT
  implicit val PlanCabeceraUpdatedFromDtoDecoder: Decoder[PlanCabeceraUpdatedFromDto] = deriveDecoder
  implicit val PlanCabeceraUpdatedFromDtoEncoder: Encoder[PlanCabeceraUpdatedFromDto] = deriveEncoder

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
  implicit val PlanCabeceraUpdateFromDtoDecoder: Decoder[PlanCabeceraUpdateFromDto] = deriveDecoder
  implicit val PlanCabeceraUpdateFromDtoEncoder: Encoder[PlanCabeceraUpdateFromDto] = deriveEncoder


  //REPONDS

  implicit val PlanCabeceraResponsesDecoder: Decoder[GetPlanCabeceraResponse] = deriveDecoder
  implicit val PlanCabeceraResponsesEncoder: Encoder[GetPlanCabeceraResponse] = deriveEncoder

}
