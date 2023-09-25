package readside.proyectionists.registrales.juicio_obn.projections

import cassandra.CassandraTypesAdapter.int
import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.juicio.domain.JuicioEvents
import consumers.registral.juicio_obn.domain.JuicioObnEvents

trait JuicioObnProjection extends UpdateReadSideProjection[JuicioObnEvents] {
  def collectionName: String = "read_side.buc_juicios_obn"

  val keys: List[(String, Object)] = List(
    "bju_identificador" -> event.juicioObnId,
    "bjd_soj_identificador" -> event.objetoId,
    "bjd_soj_tipo_objeto" -> event.tipoObjeto,
    "bjd_obn_id" -> event.obligacionId
  )
}
