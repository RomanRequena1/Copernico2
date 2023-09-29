package consumers.registral.juicio_tri.application.entities

import serialization.CbroSerialization

sealed trait JuicioDosCommands extends design_principles.actor_model.Command with JuicioDosMessage with CbroSerialization
object JuicioDosCommands {
  case class JuicioDosUpdateFromDto(
                                 juicioId: String,
                                 deliveryId: BigInt,
                                 registro: JuicioDosTri)
    extends JuicioDosCommands

  case class JuicioDosRemoveFromDto(
                             juicioId: String,
                             deliveryId: BigInt,
                             registro: JuicioDosTri)
    extends JuicioDosCommands
}
