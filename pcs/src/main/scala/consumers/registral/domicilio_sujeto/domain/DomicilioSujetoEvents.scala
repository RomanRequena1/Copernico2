package consumers.registral.domicilio_sujeto.domain

import consumers.registral.domicilio_sujeto.application.entities.{DomicilioSujetoExternalDto, DomicilioSujetoMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait DomicilioSujetoEvents extends Event with DomicilioSujetoMessage with CbroSerialization

object DomicilioSujetoEvents {
  case class DomicilioSujetoUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      domicilioId: String,
      registro: DomicilioSujetoExternalDto
  ) extends DomicilioSujetoEvents
}
