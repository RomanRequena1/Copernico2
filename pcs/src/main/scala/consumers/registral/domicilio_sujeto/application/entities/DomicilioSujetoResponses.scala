package consumers.registral.domicilio_sujeto.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait DomicilioSujetoResponses extends CbroSerialization
object DomicilioSujetoResponses {

  case class GetDomicilioSujetoResponse(registro: Option[DomicilioSujetoExternalDto] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
