package consumers.no_registral.obligacion.application.entities

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait ObligacionCommands extends Command with ObligacionMessage with CbroSerialization

object ObligacionCommands {
  case class ObligacionUpdateFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      deliveryId: BigInt,
      registro: ObligacionExternalDto,
      detallesObligacion: Seq[DetallesObligacion],
      isAdheridoDebito: Option[Boolean],
      cuota: Option[String],
      resultDmn: Option[String]
  ) extends ObligacionCommands


  case class ObligacionRemoveInfoFromObjeto(
                               deliveryId: BigInt,
                               sujetoId: String,
                               objetoId: String,
                               tipoObjeto: String,
                               obligacionId: String
                             ) extends ObligacionCommands
  case class ObligacionRemove(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      registro: ObligacionExternalDto,
      cuota:Option[String]
  ) extends ObligacionCommands

  case class ObligacionUpdateExencion(
      deliveryId: BigInt,
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      exencion: Exencion
  ) extends ObligacionCommands
}
