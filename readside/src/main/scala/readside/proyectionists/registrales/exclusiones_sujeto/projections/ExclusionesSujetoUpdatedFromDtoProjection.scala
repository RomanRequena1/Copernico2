package readside.proyectionists.registrales.exclusiones_sujeto.projections

import consumers.registral.etapas_procesales.application.entities.EtapasProcesalesExternalDto
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents
import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoExternalDto
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents

case class ExclusionesSujetoUpdatedFromDtoProjection(
    event: ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
) extends ExclusionesSujetoProjection {
  val registro: ExclusionesSujetoExternalDto = event.registro

  def bindings: List[(String, Serializable)] = List(
    "bse_accion" -> registro.BSE_ACCION,
    "bse_motivo" -> registro.BSE_MOTIVO,
    "bse_fecha_desde" -> registro.BSE_FECHA_DESDE,
    "bde_fecha_hasta" -> registro.BSE_FECHA_HASTA,
    "bse_origen" -> registro.BSE_ORIGEN,
    "bse_tramite_id" -> registro.BSE_TRAMITE_ID,
    "bse_nombre_gestion" -> registro.BSE_NOMBRE_GESTION,
    "bse_numero_gestion" -> registro.BSE_NUMERO_GESTION,
    "bse_tipo_exclusion" -> registro.BSE_TIPO_EXCLUSION
  )
}
