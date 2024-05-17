package readside.proyectionists.registrales.actividad_sujeto.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents

trait ActividadSujetoProjection extends UpdateReadSideProjection[ActividadSujetoEvents] {
  def collectionName: String = "read_side.buc_actividades_sujeto"
  val keys: List[(String, Object)] = List(
    "bat_suj_identificador" -> event.sujetoId,
    "bat_atd_id" -> event.actividadSujetoId
  )
}
