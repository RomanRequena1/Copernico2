package consumers.registral.objeto_juicio.application.entities

import serialization.CbroSerialization
import java.time.LocalDateTime


sealed trait ObjetoJuicioResponses extends CbroSerialization
object ObjetoJuicioResponses {

  case class GetObjetoJuicioResponse(registro: Option[ObjetoJuicioExternalDto] = None,
                                     fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization

}
