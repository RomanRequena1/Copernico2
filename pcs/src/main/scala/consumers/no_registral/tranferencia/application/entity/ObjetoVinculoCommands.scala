package consumers.no_registral.tranferencia.application.entity

import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait ObjetoVinculoCommands extends Command with ObjetoVinculoMessage with CbroSerialization


object ObjetoVinculoCommands {

  case class UpdateVinculoObjetoFromObj(
                                  deliveryId: BigInt,
                                  sujetoId: String,
                                  objetoId: String,
                                  tipoObj: String,
                                  tiene30Objeto: Boolean,
                                  isResponsable: Option[Boolean],
                                  estadoObj: Option[String],
                                  titularidad: Option[String]
                                ) extends ObjetoVinculoCommands {

  }

  case class CreateNewVinculoObjSujToTransf(
                                             deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObj: String,
                                          tiene30ObjetoTranf: Boolean
                                        ) extends ObjetoVinculoCommands
}