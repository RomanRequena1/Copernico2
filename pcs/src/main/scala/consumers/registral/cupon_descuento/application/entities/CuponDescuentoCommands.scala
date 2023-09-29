package consumers.registral.cupon_descuento.application.entities

import consumers.registral.cupon_descuento.application.entities.CuponDescuentoExternalDto.DetallesCuponDescuento
import design_principles.actor_model.Command
import serialization.CbroSerialization

sealed trait CuponDescuentoCommands extends Command with CuponDescuentoMessage with CbroSerialization


object CuponDescuentoCommands {
  case class CuponDescuentoUpdateFromDto(
                                      deliveryId: BigInt,
                                      sujetoId: String,
                                      objetoId: String,
                                      tipoObjeto: String,
                                      obligacionId: String,
                                      registro: DetallesCuponDescuento,
                                      detallesCuponDescuento: Seq[DetallesCuponDescuento]
                                    ) extends CuponDescuentoCommands

  case class CuponDescuentoRemove(
                               deliveryId: BigInt,
                               sujetoId: String,
                               objetoId: String,
                               tipoObjeto: String,
                               obligacionId: String,
                               registro: DetallesCuponDescuento,
                               detallesCuponDescuento: Seq[DetallesCuponDescuento]
                             ) extends CuponDescuentoCommands
}


