package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoResponses.GetObjetoVinculoResponse
import design_principles.actor_model.Query


sealed trait ObjetoVinculoQueries extends Query with ObjetoVinculoMessage

object ObjetoVinculoQueries {
  case class GetStateObjetoVinculo(objetoId: String) extends ObjetoVinculoQueries {
    override type ReturnType = GetObjetoVinculoResponse
  }
}