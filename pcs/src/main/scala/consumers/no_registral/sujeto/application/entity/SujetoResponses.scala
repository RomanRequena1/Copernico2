package consumers.no_registral.sujeto.application.entity

import java.time.LocalDateTime
import design_principles.actor_model.Response
import serialization.CbroSerialization

sealed trait SujetoResponses

object SujetoResponses {

  case class GetSujetoResponse(
      saldo: BigDecimal = 0,
      objetos: Set[String] = Set.empty, // implement Json extension for tuples here: objetos: Set[(String, String)] = Set.empty,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      registro: Option[SujetoTri] = None
  ) extends Response with CbroSerialization

}
