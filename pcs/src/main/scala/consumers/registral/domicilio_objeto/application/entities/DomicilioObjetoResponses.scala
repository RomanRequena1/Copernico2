package consumers.registral.domicilio_objeto.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait DomicilioObjetoResponses extends CbroSerialization
object DomicilioObjetoResponses {

  case class GetDomicilioObjetoResponse(registro: Option[DomicilioObjetoTri] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
