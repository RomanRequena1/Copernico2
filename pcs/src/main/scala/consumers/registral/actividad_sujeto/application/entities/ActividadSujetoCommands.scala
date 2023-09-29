package consumers.registral.actividad_sujeto.application.entities

import consumers.registral.actividad_sujeto.application.entities.ActividadSujetoExternalDto.ActividadSujeto
import design_principles.actor_model.Command
import serialization.CbroSerialization

trait ActividadSujetoCommands extends Command with ActividadSujetoMessage with CbroSerialization

object ActividadSujetoCommands {

  case class ActividadSujetoUpdateFromDto(
      sujetoId: String,
      actividadSujetoId: String,
      deliveryId: BigInt,
      registro: ActividadSujeto
  ) extends ActividadSujetoCommands
}
