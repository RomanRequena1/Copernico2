package consumers.no_registral.obligacion.domain

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, DetallesObligacionCaracteristicas, DetallesSupresiones, ObligacionExternalDto, ObligacionMessage}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait ObligacionEvents extends Event with ObligacionMessage with CbroSerialization {
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
                                          registro: Option[ObligacionExternalDto],
                                          exenta: Boolean,
                                          porcentajeExencion: BigDecimal,
                                          saldo: BigDecimal,
                                          operacion: String,
                                          resultDmn: Option[String]
  ) extends ObligacionEvents


  case class ObligacionUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionExternalDto,
      detallesObligacion: Seq[DetallesObligacion],
      detallesCaracteristicas: Seq[DetallesObligacionCaracteristicas],
      detallesSupresiones: Seq[DetallesSupresiones],
      isAdheridoDebito: Option[Boolean],
      cuota: Option[String],
      resultDmn: Option[String]
  ) extends ObligacionEvents

  case class ObligacionRemovedInfoFromObjeto(
                                deliveryId: BigInt,
                                sujetoId: String,
                                objetoId: String,
                                tipoObjeto: String,
                                obligacionId: String,
                                registro: ObligacionExternalDto,
                                cuota: Option[String]
                              ) extends ObligacionEvents
  case class ObligacionRemoved(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionExternalDto,
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

  case class ObligacionAntUpdatedFromDto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionExternalDto,
      detallesObligacion: Seq[DetallesObligacion],
      detallesCaracteristicas: Seq[DetallesObligacionCaracteristicas],
      detallesSupresiones: Seq[DetallesSupresiones],
      isAdheridoDebito: Option[Boolean],
      cuota: Option[String]
  ) extends ObligacionEvents

}
