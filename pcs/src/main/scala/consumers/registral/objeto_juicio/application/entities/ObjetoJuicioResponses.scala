package consumers.registral.objeto_juicio.application.entities

import serialization.CbroSerialization
import java.time.LocalDateTime
import design_principles.actor_model.Response


sealed trait ObjetoJuicioResponses extends CbroSerialization
object ObjetoJuicioResponses {

  case class GetObjetoJuicioResponse(registro: Option[ObjetoJuicioExternalDto] = None,
                                     lastDeliveryIdByEvent: BigInt = 0,
                                     fechaUltMod: LocalDateTime
                                    ) extends Response with CbroSerialization

}
