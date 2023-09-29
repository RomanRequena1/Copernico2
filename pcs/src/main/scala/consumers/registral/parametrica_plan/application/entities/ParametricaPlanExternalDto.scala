package consumers.registral.parametrica_plan.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime




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
  ) extends CbroSerialization

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
  ) extends CbroSerialization
