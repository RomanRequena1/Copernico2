package consumers.registral.tramite.application.entities

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait TramiteCommands extends Command with TramiteMessage with CbroSerialization

object TramiteCommands {
  case class TramiteUpdateFromDto(
      sujetoId: String,
      tramiteId: String,
      deliveryId: BigInt,
      registro: Tramite
  ) extends TramiteCommands
}
