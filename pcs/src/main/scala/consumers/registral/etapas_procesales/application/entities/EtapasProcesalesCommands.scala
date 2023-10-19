package consumers.registral.etapas_procesales.application.entities

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait EtapasProcesalesCommands extends Command with EtapasProcesalesMessage with CbroSerialization

object EtapasProcesalesCommands {
  case class EtapasProcesalesUpdateFromDto(
      juicioId: String,
      etapaId: String,
      deliveryId: BigInt,
      registro: EtapasProcesalesExternalDto
  ) extends EtapasProcesalesCommands
}
