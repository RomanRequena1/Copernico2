package readside.proyectionists.no_registrales.exclusiones_objeto.projections

import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoEvents
import cassandra.mechanism.UpdateReadSideProjection


trait ExclusionesObjetoProjection extends UpdateReadSideProjection[ExclusionesObjetoEvents] {
  def collectionName: String = "read_side.buc_exclusiones_objeto"

  val keys: List[(String, Object)] = List(
    "boe_obj_id" -> event.objetoId
  )
}
