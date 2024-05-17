package consumers.registral.tramite.domain

import consumers.registral.tramite.application.entities.{Tramite, TramiteMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class TramiteState(
    registro: Option[Tramite] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[TramiteMessage, TramiteEvents, TramiteState] with CbroSerialization
