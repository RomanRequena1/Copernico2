package consumers.registral.plan_pago.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.{SujetoAnt, SujetoTri}

import java.time.LocalDateTime
import play.api.libs.json.JsObject
import serialization.CbroSerialization

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[SujetoTri], name = "planPagoTri"),
    new JsonSubTypes.Type(value = classOf[SujetoAnt], name = "planPagoAnt"),
  )
)
sealed trait PlanPagoExternalDto extends ddd.ExternalDto with CbroSerialization {
  def EV_ID: String

  def BPL_SUJ_IDENTIFICADOR: String

  def BPL_SOJ_TIPO_OBJETO: String

  def BPL_SOJ_IDENTIFICADOR: String

  def BPL_PLN_ID: String

  def BPL_CANTIDAD_CUOTAS: Option[BigInt]

  def BPL_ESTADO: Option[String]

  def BPL_FECHA_ACT_DEUDA: Option[LocalDateTime]

  def BPL_FECHA_EMISION: Option[LocalDateTime]

  def BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal]

  def BPL_IMPORTE_ANTICIPO: Option[BigDecimal]

  def BPL_IMPORTE_FINANCIADO: Option[BigDecimal]

  def BPL_NRO_REFERENCIA: Option[String]

  def BPL_TIPO: Option[String]

  //def BPL_OTROS_ATRIBUTOS: Option[JsObject]

}

object PlanPagoExternalDto {

  case class PlanPagoTri(EV_ID: String,
                         BPL_SUJ_IDENTIFICADOR: String,
                         BPL_SOJ_TIPO_OBJETO: String,
                         BPL_SOJ_IDENTIFICADOR: String,
                         BPL_PLN_ID: String,
                         BPL_CANTIDAD_CUOTAS: Option[BigInt],
                         BPL_ESTADO: Option[String],
                         BPL_FECHA_ACT_DEUDA: Option[LocalDateTime],
                         BPL_FECHA_EMISION: Option[LocalDateTime],
                         BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                         BPL_IMPORTE_ANTICIPO: Option[BigDecimal],
                         BPL_IMPORTE_FINANCIADO: Option[BigDecimal],
                         BPL_NRO_REFERENCIA: Option[String],
                         BPL_TIPO: Option[String])
                         //BPL_OTROS_ATRIBUTOS: Option[JsObject])
    extends PlanPagoExternalDto with CbroSerialization

  case class PlanPagoAnt(EV_ID: String,
                         BPL_SUJ_IDENTIFICADOR: String,
                         BPL_SOJ_TIPO_OBJETO: String,
                         BPL_SOJ_IDENTIFICADOR: String,
                         BPL_PLN_ID: String,
                         BPL_CANTIDAD_CUOTAS: Option[BigInt],
                         BPL_ESTADO: Option[String],
                         BPL_FECHA_ACT_DEUDA: Option[LocalDateTime],
                         BPL_FECHA_EMISION: Option[LocalDateTime],
                         BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                         BPL_IMPORTE_ANTICIPO: Option[BigDecimal],
                         BPL_IMPORTE_FINANCIADO: Option[BigDecimal],
                         BPL_NRO_REFERENCIA: Option[String],
                         BPL_TIPO: Option[String])
                         //BPL_OTROS_ATRIBUTOS: Option[JsObject]) CAMBIAR, MODERNIZAR CUANDO ESTEN LOS DETALLES

    extends PlanPagoExternalDto with CbroSerialization

}
