package readside.proyectionists.registrales.componente_i.projectionists

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.componente_i.domain.ComponenteIEvents

trait ComponenteIProjection extends UpdateReadSideProjection[ComponenteIEvents] {
  def collectionName: String = "read_side.buc_componente_i"
  val keys: List[(String, Object)] = List(
    "bci_obn_identificador" -> event.obligacionId
  )
}
