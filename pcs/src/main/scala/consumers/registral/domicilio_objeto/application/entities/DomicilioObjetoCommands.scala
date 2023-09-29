package consumers.registral.domicilio_objeto.application.entities

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait DomicilioObjetoCommands extends Command with DomicilioObjetoMessage with CbroSerialization

object DomicilioObjetoCommands {
  case class DomicilioObjetoUpdateFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      domicilioId: String,
      deliveryId: BigInt,
      registro: DomicilioObjetoTri
  ) extends DomicilioObjetoCommands
}
