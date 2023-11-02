package consumers.registral.cupon_descuento.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime


  case class CuponDescuentoTri(
                              EV_ID: BigInt,
                              BOB_SUJ_IDENTIFICADOR: String,
                              BOB_SOJ_IDENTIFICADOR_2: Option[String],
                              BOB_SOJ_TIPO_OBJETO: String,
                              BOB_SOJ_IDENTIFICADOR: String,
                              BOB_OBN_ID: String,
                              BOB_CANAL_ORIGEN: Option[String],
                              BOB_OTROS_ATRIBUTOS: Option[ListDetalleCuponDescuenta],

                            ) extends CbroSerialization


  case class DetallesCuponDescuento(

                                 bdc_tipo: Option[String],
                                 bdc_baja: Option[Boolean],
                                 bdc_vencimiento:Option[LocalDateTime]
                               ) extends CbroSerialization
case class ListDetalleCuponDescuenta(
                                      BOB_DETALLES: Option[List[DetallesCuponDescuento]]
                                    ) extends CbroSerialization