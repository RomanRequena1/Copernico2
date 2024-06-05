package consumers.registral.plan_cabecera.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.registral.plan_cabecera.application.entities.PlanCabeceraExternalDto.{PlanCabeceraAnt, PlanCabeceraTri}
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[PlanCabeceraTri], name = "planCabeceraTri"),
    new JsonSubTypes.Type(value = classOf[PlanCabeceraAnt], name = "planCabeceraAnt"),
  )
)
sealed trait PlanCabeceraExternalDto extends ddd.ExternalDto with CbroSerialization {
  def EV_ID: BigInt

  def BPL_IDENTIFICADOR: String

  def BPL_IDENTIFICADOR_EXTERNO: Option[String]

  def BPL_NRO_REFERENCIA: Option[String]

  def BPL_CANTIDAD_CUOTAS: Option[Integer]

  def BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal]

  def BPL_IMPORTE_ANTICIPO: Option[BigDecimal]

  def BPL_IMPORTE_FINANCIADO: Option[BigDecimal]

  def BPL_IMPORTE_CUOTA: Option[BigDecimal]

  def BPL_ESTADO: Option[String]

  def BPL_FECHA_ACT_DEUDA: Option[LocalDateTime]

  def BPL_FECHA_EMISION: Option[LocalDateTime]

  def BPL_MODELO_CODIGO: Option[String]

  def BPL_MODELO_DESCRIPCION: Option[String]

  def BPL_MODELO_DECRETO: Option[String]

  def BPL_DECRETO_DESCRIPCION: Option[String]

  def BPL_TIPO_PLAN: Option[String]

  def BPL_CANAL_ORIGEN: Option[String]

  def BPL_SUJ_IDENTIFICADOR: Option[String]

  def BPL_SOJ_TIPO_OBJETO: Option[String]

  def BPL_SOJ_IDENTIFICADOR: Option[String]

  def BPL_CUIT_ORIGEN: Option[String]


}

object PlanCabeceraExternalDto {

  case class PlanCabeceraTri(EV_ID: BigInt,
                             BPL_IDENTIFICADOR: String,
                             BPL_IDENTIFICADOR_EXTERNO: Option[String],
                             BPL_NRO_REFERENCIA: Option[String],
                             BPL_CANTIDAD_CUOTAS: Option[Integer],
                             BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                             BPL_IMPORTE_ANTICIPO: Option[BigDecimal],
                             BPL_IMPORTE_FINANCIADO: Option[BigDecimal],
                             BPL_IMPORTE_CUOTA: Option[BigDecimal],
                             BPL_ESTADO: Option[String],
                             BPL_FECHA_ACT_DEUDA: Option[LocalDateTime],
                             BPL_FECHA_EMISION: Option[LocalDateTime],
                             BPL_MODELO_CODIGO: Option[String],
                             BPL_MODELO_DESCRIPCION: Option[String],
                             BPL_MODELO_DECRETO: Option[String],
                             BPL_DECRETO_DESCRIPCION: Option[String],
                             BPL_TIPO_PLAN: Option[String],
                             BPL_CANAL_ORIGEN: Option[String],
                             BPL_SUJ_IDENTIFICADOR: Option[String],
                             BPL_SOJ_TIPO_OBJETO: Option[String],
                             BPL_SOJ_IDENTIFICADOR: Option[String],
                             BPL_CUIT_ORIGEN: Option[String])
    extends PlanCabeceraExternalDto with CbroSerialization

  case class PlanCabeceraAnt(EV_ID: BigInt,
                             BPL_IDENTIFICADOR: String,
                             BPL_IDENTIFICADOR_EXTERNO: Option[String],
                             BPL_NRO_REFERENCIA: Option[String],
                             BPL_CANTIDAD_CUOTAS: Option[Integer],
                             BPL_IMPORTE_A_FINANCIAR: Option[BigDecimal],
                             BPL_IMPORTE_ANTICIPO: Option[BigDecimal],
                             BPL_IMPORTE_FINANCIADO: Option[BigDecimal],
                             BPL_IMPORTE_CUOTA: Option[BigDecimal],
                             BPL_ESTADO: Option[String],
                             BPL_FECHA_ACT_DEUDA: Option[LocalDateTime],
                             BPL_FECHA_EMISION: Option[LocalDateTime],
                             BPL_MODELO_CODIGO: Option[String],
                             BPL_MODELO_DESCRIPCION: Option[String],
                             BPL_MODELO_DECRETO: Option[String],
                             BPL_DECRETO_DESCRIPCION: Option[String],
                             BPL_TIPO_PLAN: Option[String],
                             BPL_CANAL_ORIGEN: Option[String],
                             BPL_SUJ_IDENTIFICADOR: Option[String],
                             BPL_SOJ_TIPO_OBJETO: Option[String],
                             BPL_SOJ_IDENTIFICADOR: Option[String],
                             BPL_CUIT_ORIGEN: Option[String])
  //BPL_OTROS_ATRIBUTOS: Option[JsObject]) CAMBIAR, MODERNIZAR CUANDO ESTEN LOS DETALLES

    extends PlanCabeceraExternalDto with CbroSerialization

}

