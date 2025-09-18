package consumers.no_registral.obligacion.application.entities

import consumers.no_registral.obligacion.application.entities.ObligacionResponses.{GetMiniObligacionResponse, GetObligacionResponse}
import design_principles.actor_model.Query

sealed trait ObligacionQueries extends Query with ObligacionMessage

object ObligacionQueries {
  case class GetStateObligacion(sujetoId: String, objetoId: String, tipoObjeto: String, obligacionId: String)
      extends ObligacionQueries
      with Query {
    override type ReturnType = GetObligacionResponse
  }

  case class GetMiniStateObligacion(sujetoId: String, objetoId: String, tipoObjeto: String, obligacionId: String)
    extends ObligacionQueries
      with Query {
    override type ReturnType = GetMiniObligacionResponse
  }

  case class GetSnapshotObligacion(sujetoId: String, objetoId: String, tipoObjeto: String, obligacionId: String)
      extends ObligacionQueries
      with Query {
    override type ReturnType = GetObligacionResponse
  }

}
