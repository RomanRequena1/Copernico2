package consumers.registral.juicio_tri.domain

import consumers.registral.juicio_tri.application.entities.{JuicioDosExternalDto, JuicioDosMessage}
import design_principles.actor_model.Event


sealed trait JuicioDosEvents extends Event with JuicioDosMessage
object JuicioDosEvents {
  case class JuicioDosUpdatedFromDto(
                                   juicioId: String,
                                   deliveryId: BigInt,
                                   registro: JuicioDosExternalDto)
    extends JuicioDosEvents

  case class JuicioDosRemovedFromDto(
                             juicioId: String,
                             deliveryId: BigInt,
                             registro: JuicioDosExternalDto)
    extends JuicioDosEvents
}
