package consumers.no_registral.tranferencia.domain

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObjetoVinculoEvent extends Event with ObjetoVinculoMessage with CbroSerialization


object ObjetoVinculoEvent {

  case class UpdatedVinculoObjetoFromObj(
                                        sujetoId: String,
                                        objetoId: String,
                                        tipoObj: String,
                                        tiene30Objeto: Boolean,
                                        isResponsable: Option[Boolean],
                                        estadoObj: Option[String],
                                        titularidad: Option[String]
                                      ) extends ObjetoVinculoEvent

  case class CreatedTransfVinculoObjetoFromObj(
                                           sujetoId: String,
                                           objetoId: String,
                                           tipoObj: String,
                                           tiene30Objeto: Boolean,
                                           isResponsable: Option[Boolean],
                                           estadoObj: Option[String],
                                           titularidad: Option[String]
                                         ) extends ObjetoVinculoEvent
}
