package consumers.no_registral.sujeto.domain

import consumers.no_registral.sujeto.application.entity.{SujetoAnt, SujetoMessage, SujetoTri}
import ddd.Deliverable
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait SujetoEvents extends Event with Deliverable with SujetoMessage with CbroSerialization{
  def sujetoId: String
}

object SujetoEvents {

  case class SujetoSnapshotPersisted(
      deliveryId: BigInt,
      sujetoId: String,
      registro: Option[SujetoTri],
      saldo: BigDecimal
  ) extends SujetoEvents

  case class SujetoUpdatedFromTri(
      deliveryId: BigInt,
      sujetoId: String,
      registro: SujetoTri
  ) extends SujetoEvents

  case class SujetoUpdatedFromAnt(
      deliveryId: BigInt,
      sujetoId: String,
      registro: SujetoAnt
  ) extends SujetoEvents

  case class SujetoUpdatedFromObjeto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      saldoObjeto: BigDecimal,
      saldoObligaciones: BigDecimal
  ) extends SujetoEvents

  case class SujetoBajaFromObjetoSet(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String
  ) extends SujetoEvents
}
