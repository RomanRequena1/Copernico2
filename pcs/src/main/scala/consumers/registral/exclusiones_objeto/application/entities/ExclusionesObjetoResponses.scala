package consumers.registral.exclusiones_objeto.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ExclusionesObjetoResponses extends CbroSerialization

object ExclusionesObjetoResponses {

  case class GetExclusionesObjetoResponse(registro: Option[ExclusionesObjetoExternalDto] = None,
                                         fechaUltMod: LocalDateTime)
    extends design_principles.actor_model.Response with CbroSerialization
}
