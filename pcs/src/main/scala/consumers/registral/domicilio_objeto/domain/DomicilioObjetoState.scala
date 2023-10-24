package consumers.registral.domicilio_objeto.domain

import consumers.registral.domicilio_objeto.application.entities.{DomicilioObjetoExternalDto, DomicilioObjetoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class DomicilioObjetoState(
                                 registro: Option[DomicilioObjetoExternalDto] = None,
                                 fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[DomicilioObjetoMessage, DomicilioObjetoEvents, DomicilioObjetoState] with CbroSerialization
