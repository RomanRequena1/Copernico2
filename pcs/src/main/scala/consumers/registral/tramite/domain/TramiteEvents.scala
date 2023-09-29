package consumers.registral.tramite.domain

import consumers.registral.tramite.application.entities.{Tramite, TramiteMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait TramiteEvents extends Event with TramiteMessage with CbroSerialization{
  def sujetoId: String
  def tramiteId: String
}

object TramiteEvents {
  case class TramiteUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      tramiteId: String,
      registro: Tramite
  ) extends TramiteEvents
}
