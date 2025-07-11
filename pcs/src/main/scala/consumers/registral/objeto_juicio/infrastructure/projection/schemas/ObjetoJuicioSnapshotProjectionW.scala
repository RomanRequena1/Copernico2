package consumers.registral.objeto_juicio.infrastructure.projection.schemas

import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioExternalDto
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents
import consumers.registral.objeto_juicio.infrastructure.projection.schemas.ObjetoJuicioProjectionW

final case class ObjetoJuicioSnapshotProjectionW(
                                                 event: ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
                                               ) extends ObjetoJuicioProjectionW {

  val registro: Option[ObjetoJuicioExternalDto] = Some(event.registro)

  val fromRegistro = registro map { registro =>
    List(
      "rjp_tipo_rel" -> registro.RJP_TIPO_REL,
      "rjp_id_externo" -> registro.RJP_ID_EXTERNO,
      "rjp_id_externo_2" -> registro.RJP_ID_EXTERNO_2,
      "rjp_estado" -> registro.RJP_ESTADO
    )
  }

  val other: List[(String, BigDecimal)] =
    List(
      //"bob_saldo" -> event.saldo
      //"bob_porcentaje_exencion" -> event.porcentajeExencion,
      //"bob_exenta" -> event.exenta
    )
  val bindings: List[(String, Serializable)] = fromRegistro match {
    case Some(fromRegistro) => fromRegistro ++ other
    case None => other
  }
}
