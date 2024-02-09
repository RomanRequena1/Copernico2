package consumers.no_registral.exclusiones_objeto.application.entities

import consumers.no_registral.exclusiones_objeto.application.entities.ExclusionesObjetoResponses.GetExclusionesObjetoResponse
import design_principles.actor_model.Query

sealed trait ExclusionesObjetoQueries extends Query with ExclusionesObjetoMessage

object ExclusionesObjetoQueries {
  case class GetStateExclusionesObjeto(
                                        objetoId: String
                                      ) extends ExclusionesObjetoQueries {
    override type ReturnType = GetExclusionesObjetoResponse
  }
}