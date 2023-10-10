package consumers.no_registral.obligacion.application.entities

import java.time.LocalDateTime
import ddd.ExternalDto
import play.api.libs.json.JsObject
import serialization.CbroSerialization


sealed trait ObligacionExternalDto extends ExternalDto {
  def EV_ID: BigInt
  def BOB_SALDO: BigDecimal
  def BOB_JUI_ID: Option[BigInt]
  def SOJ_ID_EXTERNO: Option[String]
}

  object ObligacionExternalDto {
    case class ObligacionesTri(
                                BOB_SALDO: BigDecimal,
                                BOB_SUJ_IDENTIFICADOR: String,
                                BOB_SOJ_IDENTIFICADOR_2: Option[String],
                                BOB_SOJ_TIPO_OBJETO: String,
                                BOB_SOJ_IDENTIFICADOR: String,
                                BOB_OBN_ID: String,
                                BOB_ADHERIDO_DEBITO: Option[String],
                                BOB_CANAL_ORIGEN: Option[String],
                                BOB_TPBID: Option[String],
                                BOB_CAPITAL: Option[BigDecimal],
                                BOB_CUOTA: Option[String],
                                BOB_ESTADO: Option[String],
                                BOB_CONCEPTO: Option[String],
                                BOB_FECHASANCION: Option[String],
                                BOB_FISCALIZADA: Option[String],
                                BOB_IMPUESTO: Option[String],
                                BOB_SUB_ESTADO: Option[String],
                                BOB_INDICE_INT_PUNIT: Option[String],
                                BOB_INDICE_INT_RESAR: Option[String],
                                BOB_INTERES_PUNIT: Option[BigDecimal],
                                BOB_INTERES_RESAR: Option[BigDecimal],
                                BOB_JUI_ID: Option[BigInt],
                                BOB_OTROS_ATRIBUTOS: Option[ListDetallesObligaciones],
                                BOB_PERIODO: Option[String],
                                BOB_PLN_ID: Option[String],
                                BOB_PRORROGA: Option[LocalDateTime],
                                BOB_TIPO: Option[String],
                                BOB_TOTAL: Option[BigDecimal],
                                BOB_VENCIMIENTO: Option[LocalDateTime],
                                BOB_VENCIMIENTO_2: Option[LocalDateTime],
                                BOB_OGA_ID: Option[String],
                                EV_ID: BigInt,
                                RULE_NUMBER: Option[String],
                                SOJ_ID_EXTERNO: Option[String]
                              ) extends ObligacionExternalDto with CbroSerialization

    case class ObligacionesAnt(
                                BOB_SALDO: BigDecimal,
                                BOB_SUJ_IDENTIFICADOR: String,
                                BOB_SOJ_IDENTIFICADOR_2: Option[String],
                                BOB_SOJ_TIPO_OBJETO: String,
                                BOB_SOJ_IDENTIFICADOR: String,
                                BOB_OBN_ID: String,
                                BOB_SUB_ESTADO: Option[String],
                                BOB_TPBID: Option[String],
                                BOB_ADHERIDO_DEBITO: Option[String],
                                BOB_CANAL_ORIGEN: Option[String],
                                BOB_CAPITAL: Option[BigDecimal],
                                BOB_CUOTA: Option[String],
                                BOB_ESTADO: Option[String],
                                BOB_CONCEPTO: Option[String],
                                BOB_FECHASANCION: Option[String],
                                BOB_FISCALIZADA: Option[String],
                                BOB_IMPUESTO: Option[String],
                                BOB_INDICE_INT_PUNIT: Option[String],
                                BOB_INDICE_INT_RESAR: Option[String],
                                BOB_INTERES_PUNIT: Option[BigDecimal],
                                BOB_INTERES_RESAR: Option[BigDecimal],
                                BOB_JUI_ID: Option[BigInt],
                                BOB_OTROS_ATRIBUTOS: Option[ListDetallesObligaciones],
                                BOB_PERIODO: Option[String],
                                BOB_PLN_ID: Option[String],
                                BOB_PRORROGA: Option[LocalDateTime],
                                BOB_TIPO: Option[String],
                                BOB_TOTAL: Option[BigDecimal],
                                BOB_VENCIMIENTO: Option[LocalDateTime],
                                BOB_VENCIMIENTO_2: Option[LocalDateTime],
                                BOB_OGA_ID: Option[String],
                                EV_ID: BigInt,
                                RULE_NUMBER: Option[String],
                                SOJ_ID_EXTERNO: Option[String]
                              ) extends ObligacionExternalDto with CbroSerialization

    case class ListDetallesObligaciones(BOB_DETALLES: List[DetallesObligacion]) extends ObligacionExternalDto with CbroSerialization

    case class DetallesObligacion(
                                   BOB_MUNICIPIO: Option[String],
                                   RULE_NUMBER: Option[String],
                                   BAND_30: Option[Boolean],
                                   BAND_BATCH: Option[Boolean],
                                   EV_ID: Option[BigInt],
                                   SOJ_ID_EXTERNO: Option[String]
                                 ) extends ObligacionExternalDto with CbroSerialization


}