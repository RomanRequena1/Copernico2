package consumers.registral.exclusiones_sujeto.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ExclusionesSujetoResponses extends CbroSerialization

object ExclusionesSujetoResponses {

  case class GetExclusionesSujetoResponse(registro: Option[ExclusionesSujetoExternalDto] = None,
                                         fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response with CbroSerialization
}
