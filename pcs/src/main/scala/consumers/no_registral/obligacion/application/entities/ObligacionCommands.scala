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
      detallesCaracteristicas: Seq[DetallesObligacionCaracteristicas],
      idExterno: Option[String],
      detallesSupresiones: Seq[DetallesSupresiones],
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

  case class ObligacionAntUpdateFromDto(
      sujetoId: String,
      objetoId: String,
      tipoObjeto: String,
      obligacionId: String,
      deliveryId: BigInt,
      registro: ObligacionExternalDto,
      detallesObligacion: Seq[DetallesObligacion],
      detallesCaracteristicas: Seq[DetallesObligacionCaracteristicas],
      detallesSupresiones: Seq[DetallesSupresiones],
      isAdheridoDebito: Option[Boolean],
      cuota: Option[String]
  ) extends ObligacionCommands

  case class ObligacionReprocess(
       deliveryId: BigInt,
       sujetoId: String,
       objetoId: String,
       tipoObjeto: String,
       obligacionId: String
     ) extends ObligacionCommands
}
