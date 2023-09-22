package readside.proyectionists.registrales.juicio_tri.projections

import cassandra.CassandraTypesAdapter.int
import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.juicio_tri.domain.JuicioDosEvents

trait JuicioDosProjection extends UpdateReadSideProjection[JuicioDosEvents]{
  def collectionName: String = "read_side.buc_juicios_cab"

  val keys: List[(String, Object)] = List(
    "bju_identificador" -> event.juicioId
  )
}
