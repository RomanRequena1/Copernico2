package consumers.no_registral.objeto.application.entities

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
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
                                treintaFinal: Boolean
                              ) extends ObjetoResponses with CbroSerialization

  case class GetAllObnResponse(
                                objetoId: String,
                                objetoTipo: String,
                                //saldo: BigDecimal,
                                obligaciones: Set[Obligacion] = Set.empty,
                              ) extends ObjetoResponses with CbroSerialization

  case class Obligacion(id: String,
                        saldo:Option[BigDecimal],
                        interes: Option[BigDecimal],
                        saldoInteres: Option[BigDecimal],
                        vencimiento: Option[LocalDateTime],
                        estado: Option[String])



  case class GetExencionResponse(
                                  exencion: Option[Exencion]
                                ) extends ObjetoResponses with CbroSerialization

}
