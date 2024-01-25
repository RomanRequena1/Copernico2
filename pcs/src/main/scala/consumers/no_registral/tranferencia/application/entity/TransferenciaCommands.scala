package consumers.no_registral.tranferencia.application.entity

import design_principles.actor_model.Command

sealed trait TransferenciaCommands extends Command with TranferenciaMessage


object TransferenciaCommands {

  case class CreateVinculoObjSujToTransf(
                                  deliveryId: BigInt,
                                  sujetoId: String,
                                  objetoId: String,
                                  tipoObj: String,
                                  tiene30ObjetoTranf: String
                                ) extends TransferenciaCommands {

  }
  case class UpdateVinculoObjSujToTransf(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: String
                                      ) extends TransferenciaCommands
  case class CreateNewVinculoObjSujToTransf(
                                             deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: String
                                        ) extends TransferenciaCommands
}