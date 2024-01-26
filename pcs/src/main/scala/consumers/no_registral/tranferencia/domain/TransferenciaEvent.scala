package consumers.no_registral.tranferencia.domain

import consumers.no_registral.tranferencia.application.entity.TranferenciaMessage
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait TransferenciaEvent extends Event with TranferenciaMessage with CbroSerialization


object TransferenciaEvent {

  case class CreatedVinculoObjSujToTransf(
                                        sujetoId: String,
                                        objetoId: String,
                                        tipoObj: String,
                                        tiene30ObjetoTranf: String
                                      ) extends TransferenciaEvent
  case class UpdatedVinculoObjSujToTransf(
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: String
                                        ) extends TransferenciaEvent
  case class CreatedNewVinculoObjSujToTransf(
                                           sujetoId: String,
                                           objetoId: String,
                                           tipoObj: String,
                                           tiene30ObjetoTranf: String
                                         ) extends TransferenciaEvent
}
