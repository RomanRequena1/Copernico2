package consumers.registral.plan_pago_detalles.application.entities

import serialization.CbroSerialization

sealed trait PlanPagoCommands extends design_principles.actor_model.Command with PlanPagoMessage with CbroSerialization
object PlanPagoCommands {
  case class PlanPagoUpdateFromDto(deliveryId: BigInt,
                                   planPagoId: String,
                                   tipoObjeto: String,
                                   objetoId: String,
                                   obligacionId: String,
                                   registro: PlanPagoExternalDto)
      extends PlanPagoCommands

}
