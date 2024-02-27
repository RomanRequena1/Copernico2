package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.Exencion
import consumers.no_registral.tranferencia.domain.{Vinculo, VinculoCotitular}

import java.time.LocalDateTime
import design_principles.actor_model.Response
import serialization.CbroSerialization

sealed trait ObjetoVinculoResponses extends Response

object ObjetoVinculoResponses {
  case class GetObjetoVinculoResponse(
                                objetoId: String,
                                fechaUltMod: LocalDateTime = LocalDateTime.MIN,
                                mapTransf: Map[Vinculo, VinculoCotitular] = Map.empty, //todo contiene todos los vinculos responsables que son transf  junto con el tiene30Objeto
                                mapVinculo: Map[Vinculo, VinculoCotitular] = Map.empty, //todo contiene todos los vinculos que no son transf junto con el tiene30Objeto
                                tiene30ObjetoVinculo: Boolean = false
                              ) extends ObjetoVinculoResponses with CbroSerialization


  case class GetExencionResponse(
                                  exencion: Option[Exencion]
                                ) extends ObjetoVinculoResponses with CbroSerialization

}
