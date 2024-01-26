package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import java.time.LocalDateTime
import design_principles.actor_model.Response
import serialization.CbroSerialization

sealed trait TransferenciaResponses extends Response

object TransferenciaResponses {
  case class GetTransferenciaResponse(
                                lastDeliveryIdByEvents: BigInt,
                                fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                              ) extends TransferenciaResponses with CbroSerialization


  case class GetExencionResponse(
                                  exencion: Option[Exencion]
                                ) extends TransferenciaResponses with CbroSerialization

}
