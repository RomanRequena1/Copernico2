package consumers.registral.componente_i.application.entities


import serialization.CbroSerialization

  case class ComponenteITri(
                              EV_ID: BigInt,
                              BOB_SUJ_IDENTIFICADOR: String,
                              BOB_SOJ_IDENTIFICADOR_2: Option[String],
                              BOB_SOJ_TIPO_OBJETO: String,
                              BOB_SOJ_IDENTIFICADOR: String,
                              BOB_OBN_ID: String,
                              BOB_CANAL_ORIGEN: Option[String],
                              BOB_OTROS_ATRIBUTOS: Option[ListDetallesComponenteI],

                            ) extends CbroSerialization
  case class DetallesComponenteI(
                                 sequence: Option[String],
                                 ruleDescription: Option[String],
                                 amountCalculated:Option[String],
                                 distributionId:Option[String]
                               ) extends CbroSerialization

case class ListDetallesComponenteI(BOB_DETALLES: List[DetallesComponenteI]) extends CbroSerialization