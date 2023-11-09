package consumers.registral.plan_pago_detalles.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.registral.plan_pago_detalles.application.entities.PlanPagoExternalDto.{PlanPagoAnt, PlanPagoTri}
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PlanPagoTri], name = "planPagoTri"),
    new JsonSubTypes.Type(value = classOf[PlanPagoAnt], name = "planPagoAnt"),
  )
)
sealed trait PlanPagoExternalDto extends ddd.ExternalDto with CbroSerialization {
  def EV_ID: BigInt

  def BPL_IDENTIFICADOR: String

  def BPD_SOJ_TIPO_OBJETO: String

  def BPD_SOJ_IDENTIFICADOR: String

  def BPD_OBN_ID: String

  def BPD_BOB_PERIODO: Option[LocalDateTime]

  def BPD_BOB_CUOTA: Option[String]

  def BPD_BOB_IMPUESTO: Option[String]

  def BPD_BOB_CONCEPTO: Option[String]

  def BPD_CANAL_ORIGEN: Option[String]

  def BPD_ESTADO: Option[String]

  def RULE_NUMBER: Option[String]

  def BPD_BOB_IMPORTE_A_FINANCIAR: Option[BigDecimal]

  def BPD_BOB_VENCIMIENTO: Option[LocalDateTime]

  def BPD_BOB_PRORROGA: Option[LocalDateTime]

  def BPD_BOB_TIPO: Option[String]

  def BPD_BOB_OGA_ID: Option[String]

  def BPD_BJU_IDENTIFICADOR: Option[String]

}

object PlanPagoExternalDto {

  case class PlanPagoTri(EV_ID: BigInt,
                         BPL_IDENTIFICADOR: String,
                         BPD_SOJ_TIPO_OBJETO: String,
                         BPD_SOJ_IDENTIFICADOR: String,
                         BPD_OBN_ID: String,
                         BPD_BOB_PERIODO: Option[LocalDateTime],
                         BPD_BOB_CUOTA: Option[String],
                         BPD_BOB_IMPUESTO: Option[String],
                         BPD_BOB_CONCEPTO: Option[String],
                         BPD_CANAL_ORIGEN: Option[String],
                         BPD_ESTADO: Option[String],
                         RULE_NUMBER: Option[String],
                         BPD_BOB_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                         BPD_BOB_VENCIMIENTO: Option[LocalDateTime],
                         BPD_BOB_PRORROGA: Option[LocalDateTime],
                         BPD_BOB_TIPO: Option[String],
                         BPD_BOB_OGA_ID: Option[String],
                         BPD_BJU_IDENTIFICADOR: Option[String])
    extends PlanPagoExternalDto with CbroSerialization

  case class PlanPagoAnt(EV_ID: BigInt,
                         BPL_IDENTIFICADOR: String,
                         BPD_SOJ_TIPO_OBJETO: String,
                         BPD_SOJ_IDENTIFICADOR: String,
                         BPD_OBN_ID: String,
                         BPD_BOB_PERIODO: Option[LocalDateTime],
                         BPD_BOB_CUOTA: Option[String],
                         BPD_BOB_IMPUESTO: Option[String],
                         BPD_BOB_CONCEPTO: Option[String],
                         BPD_CANAL_ORIGEN: Option[String],
                         BPD_ESTADO: Option[String],
                         RULE_NUMBER: Option[String],
                         BPD_BOB_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                         BPD_BOB_VENCIMIENTO: Option[LocalDateTime],
                         BPD_BOB_PRORROGA: Option[LocalDateTime],
                         BPD_BOB_TIPO: Option[String],
                         BPD_BOB_OGA_ID: Option[String],
                         BPD_BJU_IDENTIFICADOR: Option[String])
                         //BPL_OTROS_ATRIBUTOS: Option[JsObject]) CAMBIAR, MODERNIZAR CUANDO ESTEN LOS DETALLES

    extends PlanPagoExternalDto with CbroSerialization

}
