package consumers.registral.actividad_sujeto.domain
import consumers.registral.actividad_sujeto.application.entities.{ActividadSujeto, ActividadSujetoMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ActividadSujetoEvents extends Event with ActividadSujetoMessage with CbroSerialization {
  def sujetoId: String
  def actividadSujetoId: String
}

object ActividadSujetoEvents {

  case class ActividadSujetoUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      actividadSujetoId: String,
      registro: ActividadSujeto
  ) extends ActividadSujetoEvents
}
