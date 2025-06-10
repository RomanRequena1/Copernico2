package design_principles.actor_model.mechanism

import design_principles.actor_model.{Command, Event}

import scala.util.Try

object DeliveryIdManagement {
  def isIdempotent(command: Command, lastDeliveryIdByEvents: BigInt): Boolean = {
    command.deliveryId <= lastDeliveryIdByEvents
  }

  val interno: String = Try(System.getenv("INTERNALLY_IDEMPOTENCY")).getOrElse("OFF")

  def isIdempotentInternally(command: Command, lastDeliveryIdByEvents: BigInt): Boolean = {
    //TODO: quitar esta validación luego de truncar el writeside
    if (lastDeliveryIdByEvents == null) {
      false
    } else if (interno.equals("ON")) {
      command.deliveryId < lastDeliveryIdByEvents
    } else {
      false
    }
  }
}
