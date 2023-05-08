package readside.proyectionists.registrales.cupon_descuento.projectionists

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents

trait CuponDescuentoProjection extends UpdateReadSideProjection[CuponDescuentoEvents] {
  def collectionName: String = "read_side.buc_cupon_descuento"
  val keys: List[(String, Object)] = List(
    "bob_suj_identificador" -> event.sujetoId,
    "bob_soj_tipo_objeto" -> event.tipoObjeto,
    "bob_soj_identificador" -> event.objetoId,
    "bob_obn_id" -> event.obligacionId
  )
}
