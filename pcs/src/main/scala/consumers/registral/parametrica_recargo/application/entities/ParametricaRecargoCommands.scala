package consumers.registral.parametrica_recargo.application.entities

import serialization.CbroSerialization

sealed trait ParametricaRecargoCommands extends ParametricaRecargoMessage with design_principles.actor_model.Command with CbroSerialization
object ParametricaRecargoCommands {
  case class ParametricaRecargoUpdateFromDto(parametricaRecargoId: String,
                                             deliveryId: BigInt,
                                             registro: ParametricaRecargoExternalDto)
      extends ParametricaRecargoCommands

}
