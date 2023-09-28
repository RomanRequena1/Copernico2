package consumers.no_registral.obligacion.domain

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ObligacionExternalDto, ObligacionMessage, ObligacionesTri}
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto.DetallesObligacion
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObligacionEvents extends Event with ObligacionMessage with CbroSerialization{
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def obligacionId: String
}

object ObligacionEvents {
  val operaciones: Map[String, String] = Map(("Upsert" -> "U"), ("Delete" -> "D"),("FullDelete" -> "FD"))

  case class ObligacionPersistedSnapshot(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObjeto: String,
                                          obligacionId: String,
                                          registro: Option[ObligacionesTri],
                                          exenta: Boolean,
                                          porcentajeExencion: BigDecimal,
                                          saldo: BigDecimal,
                                          operacion: String
  ) extends ObligacionEvents

  case class ObligacionUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionesTri,
      detallesObligacion: Seq[DetallesObligacion],
      isAdheridoDebito: Option[Boolean]
  ) extends ObligacionEvents

  case class ObligacionRemoved(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionesTri,
      cuota:Option[String]
                              ) extends ObligacionEvents

  case class ObligacionAddedExencion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      exencion: Exencion
  ) extends ObligacionEvents

}
