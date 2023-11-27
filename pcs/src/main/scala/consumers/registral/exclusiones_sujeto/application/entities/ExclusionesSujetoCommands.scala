package consumers.registral.exclusiones_sujeto.application.entities

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait ExclusionesSujetoCommands extends Command with ExclusionesSujetoMessage with CbroSerialization

object ExclusionesSujetoCommands {
  case class ExclusionesSujetoUpdateFromDto(
     deliveryId: BigInt,
     sujetoId: String,
     registro: ExclusionesSujetoExternalDto
  ) extends ExclusionesSujetoCommands
}

