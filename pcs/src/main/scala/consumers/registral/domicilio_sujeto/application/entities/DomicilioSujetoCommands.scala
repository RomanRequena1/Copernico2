package consumers.registral.domicilio_sujeto.application.entities

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait DomicilioSujetoCommands extends Command with DomicilioSujetoMessage with CbroSerialization

object DomicilioSujetoCommands {
  case class DomicilioSujetoUpdateFromDto(
      sujetoId: String,
      domicilioId: String,
      deliveryId: BigInt,
      registro: DomicilioSujetoTri
  ) extends DomicilioSujetoCommands
}
