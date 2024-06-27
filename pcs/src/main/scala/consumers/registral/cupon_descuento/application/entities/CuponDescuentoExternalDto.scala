package consumers.registral.cupon_descuento.application.entities

import serialization.CbroSerialization

import java.time.LocalDateTime


  case class CuponDescuentoTri(
                              EV_ID: BigInt,
                              BCD_SUJ_IDENTIFICADOR: String,
                              BCD_SOJ_IDENTIFICADOR_2: Option[String],
                              BCD_SOJ_TIPO_OBJETO: String,
                              BCD_SOJ_IDENTIFICADOR: String,
                              BCD_OBN_IDENTIFICADOR: String,
                              BCD_CANAL_ORIGEN: Option[String],
                              BCD_OTROS_ATRIBUTOS: Option[ListDetalleCuponDescuenta],
                            ) extends CbroSerialization


  case class DetallesCuponDescuento(

                                 bcd_tipo: Option[String],
                                 bcd_baja: Option[Boolean],
                                 bcd_monto: Option[BigDecimal],
                                 bcd_descripcion: Option[String],
                                 bcd_vencimiento:Option[LocalDateTime]
                               ) extends CbroSerialization
case class ListDetalleCuponDescuenta(
                                      BCD_DETALLES: Option[List[DetallesCuponDescuento]]
                                    ) extends CbroSerialization