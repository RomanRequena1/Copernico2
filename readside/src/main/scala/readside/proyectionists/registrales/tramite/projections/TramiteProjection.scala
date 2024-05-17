package readside.proyectionists.registrales.tramite.projections
import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.tramite.domain.TramiteEvents

trait TramiteProjection extends UpdateReadSideProjection[TramiteEvents] {
  def collectionName: String = "read_side.buc_tramites"

  val keys: List[(String, String)] = List(
    "btr_suj_identificador" -> event.sujetoId,
    "btr_trm_id" -> event.tramiteId
  )
}
