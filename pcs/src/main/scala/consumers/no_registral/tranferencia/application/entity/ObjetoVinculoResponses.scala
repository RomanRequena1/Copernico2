package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import java.time.LocalDateTime
import design_principles.actor_model.Response
import serialization.CbroSerialization

sealed trait ObjetoVinculoResponses extends Response

object ObjetoVinculoResponses {
  case class GetObjetoVinculoResponse(
                                lastDeliveryIdByEvents: BigInt,
                                fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                              ) extends ObjetoVinculoResponses with CbroSerialization


  case class GetExencionResponse(
                                  exencion: Option[Exencion]
                                ) extends ObjetoVinculoResponses with CbroSerialization

}
