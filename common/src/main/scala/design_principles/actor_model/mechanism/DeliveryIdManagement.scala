package design_principles.actor_model.mechanism

import design_principles.actor_model.{Command, Event}

object DeliveryIdManagement {
  def isIdempotent(command: Command, lastDeliveryIdByEvents: BigInt): Boolean = {
    command.deliveryId <= lastDeliveryIdByEvents
  }
  def isIdempotentInternally(command: Command, lastDeliveryIdByEvents: BigInt): Boolean = {
    //TODO: quitar esta validación luego de truncar el writeside
    if (lastDeliveryIdByEvents == null) {
      false
    } else
      command.deliveryId < lastDeliveryIdByEvents
  }
}
