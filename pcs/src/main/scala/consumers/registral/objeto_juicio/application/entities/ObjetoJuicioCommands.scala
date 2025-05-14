package consumers.registral.objeto_juicio.application.entities

import serialization.CbroSerialization

sealed trait ObjetoJuicioCommands extends design_principles.actor_model.Command with ObjetoJuicioMessage with CbroSerialization
object ObjetoJuicioCommands {
  case class ObjetoJuicioUpdateFromDto(
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
                                      ) extends ObjetoJuicioCommands

  case class RemoveObjetoJuicioFromDto(
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
                                      ) extends ObjetoJuicioCommands
}
