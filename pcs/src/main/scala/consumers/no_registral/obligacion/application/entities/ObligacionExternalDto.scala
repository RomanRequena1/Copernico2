package consumers.no_registral.obligacion.application.entities

import com.fasterxml.jackson.annotation.{JsonSubTypes, JsonTypeInfo}
import ddd.ExternalDto
import serialization.CbroSerialization

import java.time.LocalDateTime

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes(
  Array(
    new JsonSubTypes.Type(value = classOf[ObligacionesTri], name = "obligacionesTri"),
    new JsonSubTypes.Type(value = classOf[ObligacionesAnt], name = "obligacionesAnt")
  )
)
sealed trait ObligacionExternalDto extends ExternalDto with CbroSerialization {
  def RULE_NUMBER: Option[String]

  def EV_ID: BigInt

  def BOB_SUJ_IDENTIFICADOR: String

  def BOB_SOJ_TIPO_OBJETO: String

  def BOB_SOJ_IDENTIFICADOR: String

  def BOB_SOJ_IDENTIFICADOR_2: Option[String]

  def BOB_OBN_ID: String

  def BOB_ADHERIDO_DEBITO: Option[String]

  def BOB_CANAL_ORIGEN: Option[String]

  def BOB_CAPITAL: Option[BigDecimal]

  def BOB_CUOTA: Option[String]

  def BOB_ESTADO: Option[String]

  def BOB_CONCEPTO: Option[String]

  def BOB_FECHASANCION: Option[String]

  def BOB_SUB_ESTADO: Option[String]

  def BOB_TPBID: Option[String]

  def BOB_FISCALIZADA: Option[String]

  def BOB_IMPUESTO: Option[String]

  def BOB_INDICE_INT_PUNIT: Option[String]

  def BOB_INDICE_INT_RESAR: Option[String]

  def BOB_INTERES_PUNIT: Option[BigDecimal]

  def BOB_INTERES_RESAR: Option[BigDecimal]

  def BOB_JUI_ID: Option[BigInt]

  def BOB_OTROS_ATRIBUTOS: Option[ListDetallesObligaciones]

  def BOB_CARACTERISTICAS: Option[ListCaracteristicasObligaciones]

  def BOB_SUPRESIONES: Option[ListDetallesSupresiones]

  def BOB_PERIODO: Option[String]

  def BOB_PLN_ID: Option[String]

  def BOB_PRORROGA: Option[LocalDateTime]

  def BOB_TIPO: Option[String]

  def BOB_SALDO: Option[BigDecimal]

  def BOB_TOTAL: Option[BigDecimal]

  def BOB_VENCIMIENTO: Option[LocalDateTime]

  def BOB_VENCIMIENTO_2: Option[LocalDateTime]

  def SOJ_ID_EXTERNO: Option[String]

  def BOB_OGA_ID: Option[String]

}

case class ObligacionesTri(
    BOB_SALDO: Option[BigDecimal],
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
    BOB_CARACTERISTICAS: Option[ListCaracteristicasObligaciones],
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
    SOJ_ID_EXTERNO: Option[String],
    BOB_SUPRESIONES: Option[ListDetallesSupresiones]
) extends ObligacionExternalDto
    with CbroSerialization

case class ObligacionesAnt(
    BOB_SALDO: Option[BigDecimal],
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
    BOB_CARACTERISTICAS: Option[ListCaracteristicasObligaciones],
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
    SOJ_ID_EXTERNO: Option[String],
    BOB_SUPRESIONES: Option[ListDetallesSupresiones]
) extends ObligacionExternalDto
    with CbroSerialization

case class ListDetallesSupresiones(BOB_DETALLES_SUPRESIONES: List[DetallesSupresiones]) extends CbroSerialization

case class DetallesSupresiones(
    BOB_DESCRIPCION: Option[String],
    BOB_TIPO_SUP: Option[String],
    BOB_ESTADO_SUP: Option[String],
    BOB_FECHA_INICIO_SUP: Option[LocalDateTime],
    BOB_FECHA_FIN_SUP: Option[LocalDateTime]
) extends CbroSerialization

case class ListDetallesObligaciones(BOB_DETALLES: List[DetallesObligacion]) extends CbroSerialization

case class ListCaracteristicasObligaciones(BOB_DETALLES_CARACTERISTICAS: List[DetallesObligacionCaracteristicas]) extends CbroSerialization

case class DetallesObligacion(
    BOB_MUNICIPIO: Option[String],
    RULE_NUMBER: Option[String],
    tiene30Obligaciones: Option[Boolean],
    BAND_BATCH: Option[Boolean],
    EV_ID: Option[BigInt],
    SOJ_ID_EXTERNO: Option[String],
    EVO_OBN_PEO_ID_MATERIAL: Option[String],
    JUICIO_MULTIOBJETO: Option[String],
    BOB_INTERES_FINANCIACION: Option[String],
    EVO_OBN_PEO_ID_FORMAL: Option[String],
    PLAN_MULTIOBJETO: Option[String],
    FLAG_OCULTA_WEB: Option[String],
    dmnNumero: Option[Int],
    dmnDescripcion: Option[String],
    SOJ_FECHA_LABRADO: Option[LocalDateTime],
    SOJ_FECHA_SENTENCIA: Option[LocalDateTime],
    SOJ_FECHA_RESOLUCION: Option[LocalDateTime],
    SOJ_DESCUENTO_VIGENTE: Option[String]
) extends CbroSerialization

case class DetallesObligacionCaracteristicas(
                                              BOB_CARACTERISTICA_CODIGO: Option[String],
                                              BOB_CARACTERISTICA_DESCRIPCION: Option[String],
                                              BOB_CARACTERISTICA_VALOR: Option[String],
                                              BOB_CARACTERISTICA_DESCRIPCION_VALOR: Option[String]
                             ) extends CbroSerialization
