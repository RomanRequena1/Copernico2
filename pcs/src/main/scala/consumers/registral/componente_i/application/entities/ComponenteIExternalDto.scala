package consumers.registral.componente_i.application.entities


import serialization.CbroSerialization

  case class ComponenteITri(
                              EV_ID: BigInt,
                              BCI_SUJ_IDENTIFICADOR: String,
                              BCI_SOJ_IDENTIFICADOR_2: Option[String],
                              BCI_SOJ_TIPO_OBJETO: String,
                              BCI_SOJ_IDENTIFICADOR: String,
                              BCI_OBN_IDENTIFICADOR: String,
                              BCI_CANAL_ORIGEN: Option[String],
                              BCI_OTROS_ATRIBUTOS: Option[ListDetallesComponenteI]

                            ) extends CbroSerialization
  case class DetallesComponenteI(
                                 sequence: Option[String],
                                 ruleDescription: Option[String],
                                 amountCalculated:Option[String],
                                 distributionId:Option[String],
                                 codigo: Option[String],
                                 concepto: Option[String],
                                 monto: Option[BigDecimal]
                               ) extends CbroSerialization

case class ListDetallesComponenteI(BCI_DETALLES: List[DetallesComponenteI]) extends CbroSerialization