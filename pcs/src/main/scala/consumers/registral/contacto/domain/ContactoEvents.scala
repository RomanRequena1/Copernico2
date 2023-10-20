package consumers.registral.contacto.domain

import serialization.CbroSerialization

sealed trait ContactoEvents extends design_principles.actor_model.Event with CbroSerialization
object ContactoEvents {
  case class ContactoUpdatedFromDto(deliveryId: BigInt, aggregateRoot: String, registro: ContactoExternalDto)
      extends ContactoEvents
}
