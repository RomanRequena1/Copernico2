package consumers.registral.tramite.application.entities

import java.time.LocalDateTime
import design_principles.actor_model.Response
import serialization.CbroSerialization

sealed trait TramiteResponses extends CbroSerialization
object TramiteResponses {

  case class GetTramiteResponse(registro: Option[Tramite] = None, fechaUltMod: LocalDateTime) extends Response with CbroSerialization
}
