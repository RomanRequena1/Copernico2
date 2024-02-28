package consumers.registral.tramite.application.entities

import design_principles.actor_model.Response
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait TramiteResponses extends CbroSerialization
object TramiteResponses {

  case class GetTramiteResponse(registro: Option[Tramite] = None, fechaUltMod: LocalDateTime) extends Response with CbroSerialization
}
