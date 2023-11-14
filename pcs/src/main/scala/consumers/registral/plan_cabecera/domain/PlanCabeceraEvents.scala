package consumers.registral.plan_cabecera.domain

import consumers.registral.plan_cabecera.application.entities.{PlanCabeceraExternalDto, PlanCabeceraMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait PlanCabeceraEvents extends Event with PlanCabeceraMessage with CbroSerialization

object PlanCabeceraEvents{
  case class PlanCabeceraUpdatedFromDto(deliveryId: BigInt,
                                        planCabeceraId: String,
                                        registro: PlanCabeceraExternalDto)
    extends PlanCabeceraEvents

  case class PlanCabeceraRemovedFromDto(deliveryId: BigInt,
                                        planCabeceraId: String,
                                        registro: PlanCabeceraExternalDto)
    extends PlanCabeceraEvents
}
