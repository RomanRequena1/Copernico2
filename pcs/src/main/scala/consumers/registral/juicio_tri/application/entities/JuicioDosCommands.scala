package consumers.registral.juicio_tri.application.entities

sealed trait JuicioDosCommands extends design_principles.actor_model.Command with JuicioDosMessage
object JuicioDosCommands {
  case class JuicioDosUpdateFromDto(
                                 juicioId: String,
                                 deliveryId: BigInt,
                                 registro: JuicioDosExternalDto)
    extends JuicioDosCommands

  case class JuicioDosRemoveFromDto(
                             juicioId: String,
                             deliveryId: BigInt,
                             registro: JuicioDosExternalDto)
    extends JuicioDosCommands
}
