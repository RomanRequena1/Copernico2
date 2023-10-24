package consumers.registral.juicio_tri.domain

import consumers.registral.juicio_tri.application.entities.{JuicioDosMessage, JuicioDosTri}
import cqrs.base_actor.typed.AbstractStateWithCQRS
import serialization.CbroSerialization

import java.time.LocalDateTime

case class JuicioDosState (
                            lastDeliveryIdByEvents: BigInt = 0,
                            registro: Option[JuicioDosTri] = None,
                            fechaUltMod: LocalDateTime = LocalDateTime.MIN
 ) extends AbstractStateWithCQRS[JuicioDosMessage, JuicioDosEvents, JuicioDosState] with CbroSerialization
