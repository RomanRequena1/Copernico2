package consumers.registral.juicio.application.entities

import akka.Done
import akka.actor.typed.ActorRef
import serialization.CbroSerialization

sealed trait JuicioCommands extends design_principles.actor_model.Command with JuicioMessage with CbroSerialization
object JuicioCommands {
  case class JuicioUpdateFromDto(sujetoId: String,
                                 objetoId: String,
                                 tipoObjeto: String,
                                 juicioId: String,
                                 deliveryId: BigInt,
                                 registro: JuicioTri,
                                 detallesJuicio: Seq[DetallesJuicio])
      extends JuicioCommands

}
