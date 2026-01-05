package consumers.no_registral.objeto.application.entities

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import design_principles.actor_model.Response
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ObjetoResponses extends Response

object ObjetoResponses {
  case class GetObjetoResponse(
                                lastDeliveryIdByEvents: BigInt,
                                saldo: BigDecimal,
                                tags: Set[String] = Set.empty,
                                obligaciones: Set[String] = Set.empty,
                                sujetos: Set[String] = Set.empty,
                                sujetoResponsable: Option[String] = None,
                                fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                                registro: Option[ObjetoExternalDto] = None,
                                exenciones: Set[Exencion],
                                bandTipo: String,
                                treinta: Boolean,
                                treintaSujeto: Option[Boolean],
                                treintaFinal: Boolean
                              ) extends ObjetoResponses with CbroSerialization

  case class GetAllObnResponse(
                                objetoId: String,
                                objetoTipo: String,
                                objetoTitularidad: Option[String],
                                //saldo: BigDecimal,
                                tiene30objeto: Option[Boolean],
                                aplicarDescuento: Option[Boolean],
                                obligaciones: Set[Obligacion] = Set.empty,
                              ) extends ObjetoResponses with CbroSerialization

  case class Obligacion(id: String,
                        saldo:Option[BigDecimal],
                        interes: Option[BigDecimal],
                        saldoInteres: Option[BigDecimal],
                        vencimiento: Option[LocalDateTime],
                        estado: Option[String],
                        tiene30obn: Option[Boolean],
                        registro: Option[ObligacionExternalDto]
                       )



  case class GetExencionResponse(
                                  exencion: Option[Exencion]
                                ) extends ObjetoResponses with CbroSerialization

}
