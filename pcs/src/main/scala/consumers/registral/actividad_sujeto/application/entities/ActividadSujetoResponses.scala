package consumers.registral.actividad_sujeto.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ActividadSujetoResponses extends CbroSerialization
object ActividadSujetoResponses {

  case class GetActividadSujetoResponse(
      registro: Option[ActividadSujetoExternalDto] = None,
      fechaUltMod: LocalDateTime
  ) extends design_principles.actor_model.Response
}
