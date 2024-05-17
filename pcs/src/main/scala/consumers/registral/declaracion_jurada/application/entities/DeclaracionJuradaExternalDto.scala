package consumers.registral.declaracion_jurada.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime


  case class DeclaracionJurada(EV_ID: String,
                               BDJ_DDJ_ID: String,
                               BDJ_SUJ_IDENTIFICADOR: String,
                               BDJ_SOJ_TIPO_OBJETO: String,
                               BDJ_SOJ_IDENTIFICADOR: String,
                               BDJ_CUOTA: Option[String],
                               BDJ_ESTADO: Option[String],
                               BDJ_FISCALIZADA: Option[String],
                               BDJ_IMPUESTO_DETERMINADO: Option[BigDecimal],
                               BDJ_OBN_ID: Option[String],
                               BDJ_OTROS_ATRIBUTOS: Option[DetalleDeclaracionJurada],
                               BDJ_PERCEPCIONES: Option[BigDecimal],
                               BDJ_PERIODO: Option[String],
                               BDJ_PRORROGA: Option[LocalDateTime],
                               BDJ_RECAUDACIONES: Option[BigDecimal],
                               BDJ_RETENCIONES: Option[BigDecimal],
                               BDJ_TIPO: Option[String],
                               BDJ_TOTAL: Option[BigDecimal],
                               BDJ_VENCIMIENTO: Option[LocalDateTime])
      extends  CbroSerialization

case class DetalleDeclaracionJurada(BDJ_DETALLES: Option[String]) extends  CbroSerialization