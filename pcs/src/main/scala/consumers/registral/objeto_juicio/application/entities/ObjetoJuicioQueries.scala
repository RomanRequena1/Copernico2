package consumers.registral.objeto_juicio.application.entities

import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioResponses.GetObjetoJuicioResponse
import design_principles.actor_model.Query

sealed trait ObjetoJuicioQueries extends Query with ObjetoJuicioMessage

object ObjetoJuicioQueries {
  case class GetStateObjetoJuicio(
      objetoId: String,
      tipoObjeto: String,
      juicioId: String,
      planId: String
  ) extends ObjetoJuicioQueries {
    override type ReturnType = GetObjetoJuicioResponse
  }
}
