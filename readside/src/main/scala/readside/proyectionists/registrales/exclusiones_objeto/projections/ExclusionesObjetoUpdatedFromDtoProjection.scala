package readside.proyectionists.registrales.exclusiones_objeto.projections

import consumers.registral.exclusiones_objeto.application.entities.ExclusionesObjetoExternalDto
import consumers.registral.exclusiones_objeto.domain.ExclusionesObjetoEvents

case class ExclusionesObjetoUpdatedFromDtoProjection(
    event: ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
) extends ExclusionesObjetoProjection {
  val registro: ExclusionesObjetoExternalDto = event.registro

  def bindings: List[(String, Serializable)] = List(
    "boe_accion" -> registro.BOE_ACCION,
    "boe_motivo" -> registro.BOE_MOTIVO,
    "boe_fecha_desde" -> registro.BOE_FECHA_DESDE,
    "boe_fecha_hasta" -> registro.BOE_FECHA_HASTA,
    "boe_origen" -> registro.BOE_ORIGEN,
    "boe_tramite_id" -> registro.BOE_TRAMITE_ID,
    "boe_nombre_gestion" -> registro.BOE_NOMBRE_GESTION,
    "boe_numero_gestion" -> registro.BOE_NUMERO_GESTION,
    "boe_tipo_exclusion" -> registro.BOE_TIPO_EXCLUSION
  )
}
