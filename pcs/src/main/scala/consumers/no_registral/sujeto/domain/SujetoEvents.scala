package consumers.no_registral.sujeto.domain

import consumers.no_registral.sujeto.application.entity.{SujetoExternalDto, SujetoMessage}
import ddd.Deliverable
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait SujetoEvents extends Event with Deliverable with SujetoMessage with CbroSerialization {
  def sujetoId: String
}

object SujetoEvents {

  case class SujetoSnapshotPersisted(
      deliveryId: BigInt,
      sujetoId: String,
      registro: Option[SujetoExternalDto],
      saldo: BigDecimal,
      tiene30Sujeto: Option[Boolean],
      exclusionSujeto: Option[String]
  ) extends SujetoEvents

  case class SujetoUpdatedFromObjetoTreintaPorciento(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      saldoObjeto: BigDecimal,
      saldoObligaciones: BigDecimal,
      clasificacionObjeto: String,
      deliveryIdObligacion: Option[BigInt]
  ) extends SujetoEvents

  case class SujetoUpdatedFromTri(
      deliveryId: BigInt,
      sujetoId: String,
      registro: SujetoExternalDto
  ) extends SujetoEvents

  case class SujetoUpdatedFromAnt(
      deliveryId: BigInt,
      sujetoId: String,
      registro: SujetoExternalDto
  ) extends SujetoEvents

  case class SujetoUpdatedFromObjeto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      saldoObjeto: BigDecimal,
      saldoObligaciones: BigDecimal,
      clasificacionObjeto: String,
      deliveryIdObligacion: Option[BigInt]
  ) extends SujetoEvents

  case class SujetoUpdatedFromObjetoAnt(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      saldoObjeto: BigDecimal,
      saldoObligaciones: BigDecimal,
      clasificacionObjeto: String
  ) extends SujetoEvents

  case class SujetoBajaFromObjetoSet(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String
  ) extends SujetoEvents
}
