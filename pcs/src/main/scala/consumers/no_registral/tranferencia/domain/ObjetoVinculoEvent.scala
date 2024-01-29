package consumers.no_registral.tranferencia.domain

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObjetoVinculoEvent extends Event with ObjetoVinculoMessage with CbroSerialization


object ObjetoVinculoEvent {

  case class CreatedObjetoVinculoFromObj(
                                        sujetoId: String,
                                        objetoId: String,
                                        tipoObj: String,
                                        tiene30ObjetoTranf: Boolean
                                      ) extends ObjetoVinculoEvent
  case class UpdatedVinculoObjSujToTransf(
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: Boolean
                                        ) extends ObjetoVinculoEvent
  case class CreatedNewVinculoObjSujToTransf(
                                           sujetoId: String,
                                           objetoId: String,
                                           tipoObj: String,
                                           tiene30ObjetoTranf: Boolean
                                         ) extends ObjetoVinculoEvent
}
