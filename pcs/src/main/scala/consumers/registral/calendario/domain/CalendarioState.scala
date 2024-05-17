package consumers.registral.calendario.domain

import consumers.registral.calendario.application.entities.{CalendarioExternalDto, CalendarioMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class CalendarioState(
    registro: Option[CalendarioExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.now
) extends AbstractStateWithCQRS[CalendarioMessage, CalendarioEvents, CalendarioState] with CbroSerialization
