package consumers.registral.parametrica_plan.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto.{SujetoAnt, SujetoTri}
import serialization.CbroSerialization

import java.time.LocalDateTime


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[SujetoTri], name = "parametricaPlanTri"),
    new JsonSubTypes.Type(value = classOf[SujetoAnt], name = "parametricaPlanAnt"),
  )
)
sealed trait ParametricaPlanExternalDto extends ddd.ExternalDto with CbroSerialization {

  def EV_ID: String
  def BPP_RDL_ID: String
  def BPP_FPM_ID: String
  def BPP_CANT_MAX_CUOTAS: BigInt
  def BPP_CANT_MIN_CUOTAS: BigInt
  def BPP_DECRETO: Option[String]
  def BPP_DIAS_VTO_CUOTAS: BigInt
  def BPP_FECHA_DESDE_DEUDA: LocalDateTime
  def BPP_FECHA_FIN: LocalDateTime
  def BPP_FECHA_HASTA_DEUDA: LocalDateTime
  def BPP_FECHA_INICIO: LocalDateTime
  def BPP_FPM_DESCRIPCION: String
  def BPP_INDICE_INT_FINANC: String
  def BPP_INDICE_INT_PUNIT: String
  def BPP_INDICE_INT_RESAR: String
  def BPP_MONTO_MAX_DEUDA: BigDecimal
  def BPP_MONTO_MIN_ANTICIPO: BigDecimal
  def BPP_MONTO_MIN_CUOTA: BigDecimal
  def BPP_MONTO_MIN_DEUDA: BigDecimal
  def BPP_PORCENTAJE_ANTICIPO: BigDecimal

}

object ParametricaPlanExternalDto {

  case class ParametricaPlanTri(
                                 EV_ID: String,
                                 BPP_RDL_ID: String,
                                 BPP_FPM_ID: String,
                                 BPP_CANT_MAX_CUOTAS: BigInt,
                                 BPP_CANT_MIN_CUOTAS: BigInt,
                                 BPP_DECRETO: Option[String],
                                 BPP_DIAS_VTO_CUOTAS: BigInt,
                                 BPP_FECHA_DESDE_DEUDA: LocalDateTime,
                                 BPP_FECHA_FIN: LocalDateTime,
                                 BPP_FECHA_HASTA_DEUDA: LocalDateTime,
                                 BPP_FECHA_INICIO: LocalDateTime,
                                 BPP_FPM_DESCRIPCION: String,
                                 BPP_INDICE_INT_FINANC: String,
                                 BPP_INDICE_INT_PUNIT: String,
                                 BPP_INDICE_INT_RESAR: String,
                                 BPP_MONTO_MAX_DEUDA: BigDecimal,
                                 BPP_MONTO_MIN_ANTICIPO: BigDecimal,
                                 BPP_MONTO_MIN_CUOTA: BigDecimal,
                                 BPP_MONTO_MIN_DEUDA: BigDecimal,
                                 BPP_PORCENTAJE_ANTICIPO: BigDecimal
                               ) extends ParametricaPlanExternalDto with CbroSerialization

  case class ParametricaPlanAnt(
                                 EV_ID: String,
                                 BPP_RDL_ID: String,
                                 BPP_FPM_ID: String,
                                 BPP_CANT_MAX_CUOTAS: BigInt,
                                 BPP_CANT_MIN_CUOTAS: BigInt,
                                 BPP_DECRETO: Option[String],
                                 BPP_DIAS_VTO_CUOTAS: BigInt,
                                 BPP_FECHA_DESDE_DEUDA: LocalDateTime,
                                 BPP_FECHA_FIN: LocalDateTime,
                                 BPP_FECHA_HASTA_DEUDA: LocalDateTime,
                                 BPP_FECHA_INICIO: LocalDateTime,
                                 BPP_FPM_DESCRIPCION: String,
                                 BPP_INDICE_INT_FINANC: String,
                                 BPP_INDICE_INT_PUNIT: String,
                                 BPP_INDICE_INT_RESAR: String,
                                 BPP_MONTO_MAX_DEUDA: BigDecimal,
                                 BPP_MONTO_MIN_ANTICIPO: BigDecimal,
                                 BPP_MONTO_MIN_CUOTA: BigDecimal,
                                 BPP_MONTO_MIN_DEUDA: BigDecimal,
                                 BPP_PORCENTAJE_ANTICIPO: BigDecimal
                               ) extends ParametricaPlanExternalDto with CbroSerialization

}
