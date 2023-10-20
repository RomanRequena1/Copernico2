package consumers.registral.juicio_tri.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait JuicioDosResponses extends CbroSerialization
object JuicioDosResponses {
  case class GetJuicioDosResponse(registro: Option[JuicioDosTri] = None,
                                  lastDeliveryIdByEvent: BigInt = 0,
                                  fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response with CbroSerialization
}
