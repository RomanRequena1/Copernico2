package readside.proyectionists.registrales.exclusiones_objeto.projections

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.exclusiones_objeto.domain.ExclusionesObjetoEvents

trait ExclusionesObjetoProjection extends UpdateReadSideProjection[ExclusionesObjetoEvents] {
  def collectionName: String = "read_side.buc_exclusiones_objeto"

  val keys: List[(String, Object)] = List(
    "boe_obj_id" -> event.objetoId
  )
}
