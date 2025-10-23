package consumers.registral.objeto_juicio.infrastructure.projection.schemas

import cassandra.mechanism.UpdateReadSideProjection
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents

trait ObjetoJuicioProjectionW extends UpdateReadSideProjection[ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto] {
  def collectionName: String = "read_side.buc_objeto_rel"
  val keys: List[(String, Object)] = List(
    "rjp_soj_identificador" -> event.objetoId,
    "rjp_soj_tipo_objeto" -> event.tipoObjeto,
    "rjp_identificador_rel" -> event.idRel,
    "rjp_tipo_objeto_rel" -> event.tipoObjetoRel
  )
}