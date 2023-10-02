package consumers.registral.domicilio_sujeto.domain

import java.time.LocalDateTime
import consumers.registral.domicilio_sujeto.application.entities.{ DomicilioSujetoMessage, DomicilioSujetoTri}
import cqrs.base_actor.typed.AbstractStateWithCQRS

case class DomicilioSujetoState(
    registro: Option[DomicilioSujetoTri] = None,
    fechaUltMod: LocalDateTime = LocalDateTime.MIN
) extends AbstractStateWithCQRS[DomicilioSujetoMessage, DomicilioSujetoEvents, DomicilioSujetoState]
