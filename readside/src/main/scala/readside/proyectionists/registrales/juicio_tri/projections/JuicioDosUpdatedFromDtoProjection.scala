package readside.proyectionists.registrales.juicio_tri.projections

import consumers.registral.juicio_tri.application.entities.{JuicioDosTri}
import consumers.registral.juicio_tri.domain.JuicioDosEvents

case class JuicioDosUpdatedFromDtoProjection(
                                            event: JuicioDosEvents.JuicioDosUpdatedFromDto
                                            ) extends JuicioDosProjection {
  val registro: JuicioDosTri = event.registro

  def bindings: List[(String, Serializable)] = List(
    "bju_jui_id" -> registro.BJU_JUI_ID,
    "bju_suj_identificador" -> registro.BJU_SUJ_IDENTIFICADOR,
    "bju_soj_identificador" -> registro.BJU_SOJ_IDENTIFICADOR,
    "bju_soj_tipo_objeto" -> registro.BJU_SOJ_TIPO_OBJETO,
    "bju_nro_externo" -> registro.BJU_NRO_EXTERNO,
    "bju_capital" -> registro.BJU_CAPITAL,
    "bju_estado" -> registro.BJU_ESTADO,
    "bju_fecha_generacion" -> registro.BJU_FECHA_GENERACION,
    "bju_fecha_impresion" -> registro.BJU_FECHA_IMPRESION,
    "bju_tipo" -> registro.BJU_TIPO,
    "bju_total" -> registro.BJU_TOTAL,
    "bju_tipo_juicio" -> registro.BJU_TIPO_JUICIO,
    "bju_cuit_origen" -> registro.BJU_CUIT_ORIGEN,
    "bju_ipo_id" -> registro.BJU_IPO_ID,
    "bju_canal_origen" -> registro.BJU_CANAL_ORIGEN
  )
}
