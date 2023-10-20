package consumers.registral.juicio_tri.domain

import consumers.registral.juicio_tri.application.entities.{JuicioDosMessage, JuicioDosTri}
import design_principles.actor_model.Event
import serialization.CbroSerialization


sealed trait JuicioDosEvents extends Event with JuicioDosMessage with CbroSerialization
object JuicioDosEvents {
  case class JuicioDosUpdatedFromDto(
                                   juicioId: String,
                                   deliveryId: BigInt,
                                   registro: JuicioDosTri)
    extends JuicioDosEvents

  case class JuicioDosRemovedFromDto(
                             juicioId: String,
                             deliveryId: BigInt,
                             registro: JuicioDosTri)
    extends JuicioDosEvents
}
