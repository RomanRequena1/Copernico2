package readside.proyectionists.registrales.objeto_juicio.projections

import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents

case class ObjetoJuicioUpdatedFromDtoProjection(
    event: ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
) extends ObjetoJuicioProjection {


  def bindings: List[(String, Serializable)] = List(
    "rjp_tipo_rel" -> event.tipoRel,
    "rjp_id_externo" -> event.idExterno,
    "rjp_id_externo_2" -> event.idExterno2,
    "rjp_estado" -> event.estado
  )
}
