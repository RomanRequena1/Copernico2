package consumers.registral.exclusiones_objeto.application.entities

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait ExclusionesObjetoCommands extends Command with ExclusionesObjetoMessage with CbroSerialization

object ExclusionesObjetoCommands {
  case class ExclusionesObjetoUpdateFromDto(
     deliveryId: BigInt,
     objetoId: String,
     registro: ExclusionesObjetoExternalDto
  ) extends ExclusionesObjetoCommands
}

