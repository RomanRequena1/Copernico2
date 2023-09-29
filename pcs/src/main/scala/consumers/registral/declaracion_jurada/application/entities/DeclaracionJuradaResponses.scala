package consumers.registral.declaracion_jurada.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait DeclaracionJuradaResponses extends CbroSerialization
object DeclaracionJuradaResponses {

  case class GetDeclaracionJuradaResponse(registro: Option[DeclaracionJurada] = None,
                                          fechaUltMod: LocalDateTime)
      extends design_principles.actor_model.Response
}
