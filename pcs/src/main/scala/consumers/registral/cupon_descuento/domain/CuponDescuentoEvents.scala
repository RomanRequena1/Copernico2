package consumers.registral.cupon_descuento.domain

import consumers.registral.cupon_descuento.application.entities.{CuponDescuentoMessage, CuponDescuentoTri, DetallesCuponDescuento}
import design_principles.actor_model.Event
import serialization.CbroSerialization

sealed trait CuponDescuentoEvents extends Event with CuponDescuentoMessage with CbroSerialization{
  def sujetoId: String
  def objetoId: String
  def tipoObjeto: String
  def obligacionId: String
}


object CuponDescuentoEvents {

  case class CuponDescuentoPersistedSnapshot(
                                          deliveryId: BigInt,
                                          sujetoId: String,
                                          objetoId: String,
                                          tipoObjeto: String,
                                          obligacionId: String,
                                          registro: Option[CuponDescuentoTri],
                                        ) extends CuponDescuentoEvents

  case class CuponDescuentoUpdatedFromDto(
                                       deliveryId: BigInt,
                                       sujetoId: String,
                                       objetoId: String,
                                       tipoObjeto: String,
                                       obligacionId: String,
                                       registro: CuponDescuentoTri,
                                       detallesCuponDescuento: Seq[DetallesCuponDescuento],
                                     ) extends CuponDescuentoEvents

  case class CuponDescuentoRemoved(
                                deliveryId: BigInt,
                                sujetoId: String,
                                objetoId: String,
                                tipoObjeto: String,
                                obligacionId: String,
                                registro: CuponDescuentoTri,
                              ) extends CuponDescuentoEvents
}