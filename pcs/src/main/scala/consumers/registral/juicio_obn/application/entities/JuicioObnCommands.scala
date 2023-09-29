package consumers.registral.juicio_obn.application.entities

import serialization.CbroSerialization

sealed trait JuicioObnCommands extends design_principles.actor_model.Command with JuicioObnMessage with CbroSerialization


object JuicioObnCommands {
  case class  JuicioObnUpdateFromDto(
                                      deliveryId: BigInt,
                                      juicioObnId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      obligacionId: String,
                                      registro: JuicioObnTri
                                    ) extends JuicioObnCommands

  case class JuicioObnDeleteFromDto(
                                     juicioObnId: String,
                                     objetoId: String,
                                     tipoObjeto: String,
                                     obligacionId: String,
                                     deliveryId: BigInt,
                                     registro: JuicioObnTri
                                   ) extends JuicioObnCommands
}
