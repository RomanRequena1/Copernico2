package consumers.registral.parametrica_recargo.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ParametricaRecargoResponses extends CbroSerialization
object ParametricaRecargoResponses {

  case class GetParametricaRecargoResponse(registro: Option[ParametricaRecargoTri] = None,
                                           fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response with CbroSerialization
}
