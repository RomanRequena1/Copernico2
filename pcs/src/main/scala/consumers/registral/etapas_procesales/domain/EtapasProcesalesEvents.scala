package consumers.registral.etapas_procesales.domain

import consumers.registral.etapas_procesales.application.entities.{EtapasProcesalesExternalDto, EtapasProcesalesMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait EtapasProcesalesEvents extends Event with EtapasProcesalesMessage with CbroSerialization{
  def juicioId: String
  def etapaId: String
}

object EtapasProcesalesEvents {
  case class EtapasProcesalesUpdatedFromDto(
      deliveryId: BigInt,
      juicioId: String,
      etapaId: String,
      registro: EtapasProcesalesExternalDto
  ) extends EtapasProcesalesEvents

}
