package consumers.registral.juicio_tri.domain

import consumers.registral.juicio_tri.application.entities.{JuicioDosExternalDto, JuicioDosMessage}
import cqrs.base_actor.typed.AbstractStateWithCQRS

import java.time.LocalDateTime

case class JuicioDosState (
                            lastDeliveryIdByEvents: BigInt = 0,
                            registro: Option[JuicioDosExternalDto] = None,
                            fechaUltMod: LocalDateTime = LocalDateTime.MIN
 ) extends AbstractStateWithCQRS[JuicioDosMessage, JuicioDosEvents, JuicioDosState]
