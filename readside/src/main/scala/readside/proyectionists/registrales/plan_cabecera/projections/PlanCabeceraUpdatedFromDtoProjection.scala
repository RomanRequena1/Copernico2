package readside.proyectionists.registrales.plan_cabecera.projections

import consumers.registral.plan_cabecera.application.entities.PlanCabeceraExternalDto
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents

case class PlanCabeceraUpdatedFromDtoProjection(
    event: PlanCabeceraEvents.PlanCabeceraUpdatedFromDto
) extends PlanCabeceraProjection {

  val registro: PlanCabeceraExternalDto = event.registro

  def bindings: List[(String,Serializable)] = List(
    "bpl_identificador_externo" -> registro.BPL_IDENTIFICADOR_EXTERNO,
    "bpl_nro_referencia" -> registro.BPL_NRO_REFERENCIA,
    "bpl_cantidad_cuotas" -> registro.BPL_CANTIDAD_CUOTAS,
    "bpl_importe_a_financiar" -> registro.BPL_IMPORTE_A_FINANCIAR,
    "bpl_importe_anticipo" -> registro.BPL_IMPORTE_ANTICIPO,
    "bpl_importe_financiado" -> registro.BPL_IMPORTE_FINANCIADO,
    "bpl_importe_cuota" -> registro.BPL_IMPORTE_CUOTA,
    "bpl_estado" -> registro.BPL_ESTADO,
    "bpl_fecha_act_deuda" -> registro.BPL_FECHA_ACT_DEUDA,
    "bpl_fecha_emision" -> registro.BPL_FECHA_EMISION,
    "bpl_modelo_codigo" -> registro.BPL_MODELO_CODIGO,
    "bpl_modelo_descripcion" -> registro.BPL_MODELO_DESCRIPCION,
    "bpl_modelo_decreto" -> registro.BPL_MODELO_DECRETO,
    "bpl_decreto_descripcion" -> registro.BPL_DECRETO_DESCRIPCION,
    "bpl_tipo_plan" -> registro.BPL_TIPO_PLAN,
    "bpl_canal_origen" -> registro.BPL_CANAL_ORIGEN,
    "bpl_suj_identificador" -> registro.BPL_SUJ_IDENTIFICADOR,
    "bpl_soj_tipo_objeto" -> registro.BPL_SOJ_TIPO_OBJETO,
    "bpl_soj_identificador" -> registro.BPL_SOJ_IDENTIFICADOR,
    "bpl_cuit_origen" -> registro.BPL_CUIT_ORIGEN
  )
}
