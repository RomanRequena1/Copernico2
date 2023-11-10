package consumers.registral.plan_pago_detalles.domain

import consumers.registral.plan_pago_detalles.application.entities.{PlanPagoExternalDto, PlanPagoMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait PlanPagoEvents extends Event with PlanPagoMessage with CbroSerialization

object PlanPagoEvents {
  case class PlanPagoUpdatedFromDto(deliveryId: BigInt,
                                    planPagoId: String,
                                    tipoObjeto: String,
                                    objetoId: String,
                                    obligacionId: String,
                                    registro: PlanPagoExternalDto)
    extends PlanPagoEvents

  case class PlanPagoRemovedFromDto(deliveryId: BigInt,
                                    planPagoId: String,
                                    tipoObjeto: String,
                                    objetoId: String,
                                    obligacionId: String,
                                    registro: PlanPagoExternalDto)
    extends PlanPagoEvents
}
