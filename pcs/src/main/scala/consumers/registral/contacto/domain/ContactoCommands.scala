package consumers.registral.contacto.domain

import serialization.CbroSerialization

sealed trait ContactoCommands extends design_principles.actor_model.Command with CbroSerialization

object ContactoCommands {
  case class ContactoUpdateFromDto(deliveryId: BigInt, aggregateRoot: String, registro: ContactoExternalDto)
      extends ContactoCommands
}
