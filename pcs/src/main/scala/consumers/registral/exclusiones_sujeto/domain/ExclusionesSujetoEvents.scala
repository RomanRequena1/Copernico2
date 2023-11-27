package consumers.registral.exclusiones_sujeto.domain

import consumers.registral.exclusiones_sujeto.application.entities.{ExclusionesSujetoExternalDto, ExclusionesSujetoMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ExclusionesSujetoEvents extends Event with ExclusionesSujetoMessage with CbroSerialization


object ExclusionesSujetoEvents {

  case class ExclusionesSujetoUpdatedFromDto(
     deliveryId: BigInt,
     sujetoId: String,
     registro: ExclusionesSujetoExternalDto)
    extends ExclusionesSujetoEvents

}