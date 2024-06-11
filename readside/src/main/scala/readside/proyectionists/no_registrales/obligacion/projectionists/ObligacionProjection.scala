package readside.proyectionists.no_registrales.obligacion.projectionists

import cassandra.mechanism.UpdateReadSideProjection
import consumers.no_registral.obligacion.domain.ObligacionEvents

trait ObligacionProjection extends UpdateReadSideProjection[ObligacionEvents.ObligacionPersistedSnapshot] {
  def collectionName: String = "read_side.buc_obligaciones"
  val keys: List[(String, Object)] = List(
    "bob_soj_identificador" -> event.objetoId,
    "bob_soj_tipo_objeto" -> event.tipoObjeto,
    "bob_periodo" -> event.registro.get.BOB_PERIODO,
    "bob_cuota" -> event.registro.get.BOB_CUOTA,
    "bob_obn_id" -> event.obligacionId
  )
}
