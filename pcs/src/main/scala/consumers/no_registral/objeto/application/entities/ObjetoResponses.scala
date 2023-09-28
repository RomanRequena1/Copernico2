package consumers.no_registral.objeto.application.entities

import java.time.LocalDateTime
import design_principles.actor_model.Response
import serialization.CbroSerialization

sealed trait ObjetoResponses extends Response

object ObjetoResponses {
  case class GetObjetoResponse(
      lastDeliveryIdByEvents: BigInt,
      saldo: BigDecimal,
      tags: Set[String] = Set.empty,
      obligaciones: Set[String] = Set.empty, // implement Json extension for tuples here: objetos: Set[(String, String)] = Set.empty,
      sujetos: Set[String] = Set.empty,
      sujetoResponsable: Option[String] = None,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      registro: Option[ObjetosTri] = None,
      exenciones: Set[Exencion]
  ) extends ObjetoResponses with CbroSerialization

}
  case class GetExencionResponse(
      exencion: Option[Exencion]
  ) extends ObjetoResponses with CbroSerialization


