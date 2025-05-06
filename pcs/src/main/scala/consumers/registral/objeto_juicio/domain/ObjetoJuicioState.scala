package consumers.registral.objeto_juicio.domain

import consumers.registral.objeto_juicio.application.entities.{ObjetoJuicioExternalDto, ObjetoJuicioMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization
import java.time.LocalDateTime

case class ObjetoJuicioState(
    registro: Option[ObjetoJuicioExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[ObjetoJuicioMessage, ObjetoJuicioEvents, ObjetoJuicioState] with CbroSerialization
