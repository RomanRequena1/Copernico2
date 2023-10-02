package consumers.registral.cupon_descuento.application.entities

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
                                      registro: CuponDescuentoTri,
                                      detallesCuponDescuento: Seq[DetallesCuponDescuento]
                                    ) extends CuponDescuentoCommands

  case class CuponDescuentoRemove(
                               deliveryId: BigInt,
                               sujetoId: String,
                               objetoId: String,
                               tipoObjeto: String,
                               obligacionId: String,
                               registro: CuponDescuentoTri,
                               detallesCuponDescuento: Seq[DetallesCuponDescuento]
                             ) extends CuponDescuentoCommands
}


