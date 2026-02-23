package consumers.no_registral.sujeto.application.entity

import ddd.Deliverable
import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait SujetoCommands extends Command with SujetoMessage with Deliverable with CbroSerialization

object SujetoCommands {

  case class SujetoUpdateFromTri(
      deliveryId: BigInt,
      sujetoId: String,
      registro: SujetoExternalDto
  ) extends SujetoCommands

  case class SujetoUpdateFromObjetoTreintaPorciento(
                                                     deliveryId: BigInt,
                                                     sujetoId: String,
                                                     objetoId: String,
                                                     tipoObjeto: String,
                                                     saldoObjeto: BigDecimal,
                                                     saldoObligaciones: BigDecimal,
                                                     clasificacionObjeto: String,
                                                     idExterno: Option[String],
                                                     deliveryIdObligacion: Option[BigInt]
                                                   ) extends SujetoCommands

  case class SujetoUpdateFromAnt(
      deliveryId: BigInt,
      sujetoId: String,
      registro: SujetoExternalDto
  ) extends SujetoCommands

  case class SujetoUpdateFromObjeto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      saldoObjeto: BigDecimal,
      saldoObligaciones: BigDecimal,
      clasificacionObjeto: String,
      idExterno: Option[String],
      deliveryIdObligacion: Option[BigInt]
  ) extends SujetoCommands

  case class SujetoUpdateFromObjetoAnt(
                                     deliveryId: BigInt,
                                     sujetoId: String,
                                     objetoId: String,
                                     tipoObjeto: String,
                                     saldoObjeto: BigDecimal,
                                     saldoObligaciones: BigDecimal,
                                     clasificacionObjeto: String
                                   ) extends SujetoCommands

  case class SujetoSetBajaFromObjeto(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String
  ) extends SujetoCommands
  case class SujetoRemoveObjeto(
                                 deliveryId: BigInt,
                                 sujetoId: String,
                                 objetoId: String,
                                 tipoObjeto: String
                               ) extends SujetoCommands
}
