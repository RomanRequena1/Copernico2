package consumers.no_registral.tranferencia.application.entity

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait ObjetoVinculoCommands extends Command with ObjetoVinculoMessage with CbroSerialization


object ObjetoVinculoCommands {

  case class CreateObjetoVinculoFromObj(
                                  deliveryId: BigInt,
                                  sujetoId: String,
                                  objetoId: String,
                                  tipoObj: String,
                                  tiene30Objeto: Boolean
                                ) extends ObjetoVinculoCommands {

  }
  case class UpdateVinculoObjSujToTransf(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: Boolean
                                      ) extends ObjetoVinculoCommands
  case class CreateNewVinculoObjSujToTransf(
                                             deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: Boolean
                                        ) extends ObjetoVinculoCommands
}