package consumers.registral.juicio.application.entities

import java.time.LocalDateTime
import consumers.registral.juicio.application.entities.JuicioExternalDto.DetallesJuicio
import serialization.CbroSerialization

sealed trait JuicioResponses extends CbroSerialization
object JuicioResponses {

  case class GetJuicioResponse(registro: Option[JuicioTri] = None,
                               detallesJuicio: Seq[DetallesJuicio],
                               fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization

}
