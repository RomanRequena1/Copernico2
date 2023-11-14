package consumers.registral.plan_cabecera.application.entities

import serialization.CbroSerialization

sealed trait PlanCabeceraCommands extends design_principles.actor_model.Command with PlanCabeceraMessage with CbroSerialization
object PlanCabeceraCommands{
  case class PlanCabeceraUpdateFromDto(deliveryId: BigInt,
                                       planCabeceraId: String,
                                       registro: PlanCabeceraExternalDto)
    extends PlanCabeceraCommands

  case class PlanCabeceraRemoveFromDto(deliveryId: BigInt,
                                       planCabeceraId: String,
                                       registro: PlanCabeceraExternalDto)
    extends PlanCabeceraCommands

}
