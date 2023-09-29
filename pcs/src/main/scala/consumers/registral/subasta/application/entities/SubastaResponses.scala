package consumers.registral.subasta.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait SubastaResponses extends CbroSerialization
object SubastaResponses {

  case class GetSubastaResponse(registro: Option[SubastaExternalDto] = None, fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
