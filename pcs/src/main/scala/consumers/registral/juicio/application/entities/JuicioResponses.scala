package consumers.registral.juicio.application.entities

import consumers.registral.juicio.application.entities.JuicioExternalDto.DetallesJuicio
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait JuicioResponses extends CbroSerialization
object JuicioResponses {

  case class GetJuicioResponse(registro: Option[JuicioExternalDto] = None,
                               detallesJuicio: Seq[DetallesJuicio],
                               fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization

}
