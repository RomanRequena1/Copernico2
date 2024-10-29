package readside.proyectionists.no_registrales.obligacion.projectionists

import cassandra.mechanism.UpdateReadSideProjection
import consumers.no_registral.obligacion.domain.ObligacionEvents

trait ObligacionProjection extends UpdateReadSideProjection[ObligacionEvents.ObligacionPersistedSnapshot] {
  def collectionName: String = "read_side.buc_obligaciones"
  val keys: List[(String, Object)] = List(
    //TODO pk:  si cambia la pk de la tabla, reordenar
    "bob_soj_identificador" -> event.objetoId,
    "bob_soj_tipo_objeto" -> event.tipoObjeto,
    "bob_obn_id" -> event.obligacionId,
    "bob_suj_identificador" -> event.sujetoId
  )
}
