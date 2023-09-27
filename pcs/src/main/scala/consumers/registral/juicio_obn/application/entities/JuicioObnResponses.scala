package consumers.registral.juicio_obn.application.entities

import design_principles.actor_model.Response

import java.time.LocalDateTime


sealed trait JuicioObnResponses

object JuicioObnResponses {

  case class GetJuicioObnResponses(
                                  registro: Option[JuicioObnTri] = None,
                                  lastDeliveryIdByEvent: BigInt = 0,
                                  fechasUltMod: LocalDateTime
                                  ) extends Response

}
