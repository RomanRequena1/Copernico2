package consumers.no_registral.sujeto.application.entity

import consumers.no_registral.objeto.application.entities.ObjetoQueries.GetAllObnObjeto
import consumers.no_registral.objeto.application.entities.ObjetoResponses.GetAllObnResponse
import design_principles.actor_model.Response
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait SujetoResponses

object SujetoResponses {

  case class GetSujetoResponse(
      saldo: BigDecimal = 0,
      objetos: Set[String] = Set.empty, // implement Json extension for tuples here: objetos: Set[(String, String)] = Set.empty,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      registro: Option[SujetoExternalDto] = None,
      treinta: Boolean
  ) extends Response with CbroSerialization

  case class GetAllObnSujetoResponse(
                                saldo: BigDecimal = 0,
                                objetos: Set[GetAllObnResponse] = Set.empty,
                                fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                                registro: Option[SujetoExternalDto] = None,
                                treinta: Boolean
    ) extends Response with CbroSerialization

}
