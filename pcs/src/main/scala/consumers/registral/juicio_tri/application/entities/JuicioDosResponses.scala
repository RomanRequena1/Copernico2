package consumers.registral.juicio_tri.application.entities

import java.time.LocalDateTime

sealed trait JuicioDosResponses
object JuicioDosResponses {
  case class GetJuicioDosResponse(registro: Option[JuicioDosExternalDto] = None,
                                  lastDeliveryIdByEvent: BigInt = 0,
                                  fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response
}
