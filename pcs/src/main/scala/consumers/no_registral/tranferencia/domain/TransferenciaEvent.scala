package consumers.no_registral.tranferencia.domain

import design_principles.actor_model.Event

sealed trait TransferenciaEvent extends Event


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
