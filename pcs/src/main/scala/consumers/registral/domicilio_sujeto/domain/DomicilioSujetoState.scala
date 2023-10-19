package consumers.registral.domicilio_sujeto.domain

import consumers.registral.domicilio_sujeto.application.entities.{DomicilioSujetoExternalDto, DomicilioSujetoMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS

import java.time.LocalDateTime

case class DomicilioSujetoState(
    registro: Option[DomicilioSujetoExternalDto] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[DomicilioSujetoMessage, DomicilioSujetoEvents, DomicilioSujetoState]
