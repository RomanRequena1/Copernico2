package consumers.registral.objeto_juicio.domain

import consumers.registral.objeto_juicio.application.entities.{ObjetoJuicioExternalDto, ObjetoJuicioMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObjetoJuicioEvents extends Event with ObjetoJuicioMessage with CbroSerialization

object ObjetoJuicioEvents {
  case class ObjetoJuicioUpdatedFromDto(
      deliveryId: BigInt,
      objetoId: String,
      tipoObjeto: String,
      juicioId: String,
      planId: String,
      registro: ObjetoJuicioExternalDto,
  ) extends ObjetoJuicioEvents
}
