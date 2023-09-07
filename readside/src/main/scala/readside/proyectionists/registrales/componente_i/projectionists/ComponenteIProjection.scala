package readside.proyectionists.registrales.componente_i.projectionists

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.componente_i.domain.ComponenteIEvents
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents

trait ComponenteIProjection extends UpdateReadSideProjection[ComponenteIEvents] {
  def collectionName: String = "read_side.buc_componente_i"
  val keys: List[(String, Object)] = List(
    "bci_suj_identificador" -> event.sujetoId,
    "bci_soj_tipo_objeto" -> event.tipoObjeto,
    "bci_soj_identificador" -> event.objetoId,
    "bci_obn_identificador" -> event.obligacionId
  )
}
