package consumers.registral.juicio_tri.domain

import consumers.registral.juicio.application.entities.JuicioTri
import consumers.registral.juicio_tri.application.entities.JuicioDosMessage
import cqrs.base_actor.typed.AbstractStateWithCQRS

import java.time.LocalDateTime

case class JuicioDosState (
                            lastDeliveryIdByEvents: BigInt = 0,
                            registro: Option[JuicioTri] = None,
                            fechaUltMod: LocalDateTime = LocalDateTime.MIN
 ) extends AbstractStateWithCQRS[JuicioDosMessage, JuicioDosEvents, JuicioDosState]
