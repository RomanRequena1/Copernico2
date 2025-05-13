package consumers.registral.objeto_juicio.domain

import consumers.registral.objeto_juicio.application.entities.{ObjetoJuicioExternalDto, ObjetoJuicioMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization


sealed trait ObjetoJuicioEvents extends Event with ObjetoJuicioMessage with CbroSerialization

object ObjetoJuicioEvents {
  case class ObjetoJuicioUpdatedFromDto(
                                         objetoId: String,
                                         tipoObjeto: String,
                                         idRel: String,
                                         tipoObjetoRel: String,
                                         tipoRel: String,
                                         idExterno: String,
                                         idExterno2: String,
                                         estado: String,
                                         deliveryId: BigInt,
                                         registro: ObjetoJuicioExternalDto
                                       ) extends ObjetoJuicioEvents

  case class ObjetoJuicioRemovedFromDto(
                                         objetoId: String,
                                         tipoObjeto: String,
                                         idRel: String,
                                         tipoObjetoRel: String,
                                         tipoRel: String,
                                         idExterno: String,
                                         idExterno2: String,
                                         estado: String,
                                         deliveryId: BigInt,
                                         registro: ObjetoJuicioExternalDto
                                       ) extends ObjetoJuicioEvents
}





