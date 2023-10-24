package consumers.registral.actividad_sujeto.domain

import java.time.LocalDateTime
import consumers.registral.actividad_sujeto.application.entities.{ActividadSujeto, ActividadSujetoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

case class ActividadSujetoState(
    registro: Option[ActividadSujeto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[ActividadSujetoMessage, ActividadSujetoEvents, ActividadSujetoState] with CbroSerialization
