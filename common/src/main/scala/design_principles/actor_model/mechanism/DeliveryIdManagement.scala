package design_principles.actor_model.mechanism

import design_principles.actor_model.{Command, Event}

object DeliveryIdManagement {
  def isIdempotent(command: Command, lastDeliveryIdByEvents: BigInt): Boolean = {
    command.deliveryId <= lastDeliveryIdByEvents
  }
  def isIdempotentInternally(command: Command, lastDeliveryIdByEvents: BigInt): Boolean = {
    if (lastDeliveryIdByEvents == null) {
      true
    } else
      command.deliveryId < lastDeliveryIdByEvents
  }
}
