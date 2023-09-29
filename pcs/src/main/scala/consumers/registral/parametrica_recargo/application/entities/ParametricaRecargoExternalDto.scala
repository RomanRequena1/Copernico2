package consumers.registral.parametrica_recargo.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime




  case class ParametricaRecargoTri(EV_ID: String,
                                   BPR_INDICE: String,
                                   BPR_TIPO_INDICE: String,
                                   BPR_DESCRIPCION: Option[String],
                                   BPR_FECHA_DESDE: LocalDateTime,
                                   BPR_FECHA_HASTA: Option[LocalDateTime],
                                   BPR_VALOR: Option[BigDecimal],
                                   BPR_IMPUESTO: String,
                                   BPR_CONCEPTO: String,
                                   BPR_PERIODO: String)
      extends CbroSerialization

  case class ParametricaRecargoAnt(EV_ID: String,
                                   BPR_INDICE: String,
                                   BPR_TIPO_INDICE: String,
                                   BPR_DESCRIPCION: Option[String],
                                   BPR_FECHA_DESDE: LocalDateTime,
                                   BPR_FECHA_HASTA: Option[LocalDateTime],
                                   BPR_VALOR: Option[BigDecimal],
                                   BPR_IMPUESTO: String,
                                   BPR_CONCEPTO: String,
                                   BPR_PERIODO: String)
      extends CbroSerialization
