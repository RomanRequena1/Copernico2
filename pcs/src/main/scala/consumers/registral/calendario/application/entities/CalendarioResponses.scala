package consumers.registral.calendario.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait CalendarioResponses extends CbroSerialization
object CalendarioResponses {

  case class GetCalendarioResponse(registro: Option[CalendarioExternalDto] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
