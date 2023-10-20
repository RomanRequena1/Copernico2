package consumers.registral.calendario.application.entities

import serialization.CbroSerialization

sealed trait CalendarioCommands extends CalendarioMessage with design_principles.actor_model.Command with CbroSerialization
object CalendarioCommands {
  case class CalendarioUpdateFromDto(calendarioId: String, deliveryId: BigInt, registro: CalendarioExternalDto)
      extends CalendarioCommands

}
