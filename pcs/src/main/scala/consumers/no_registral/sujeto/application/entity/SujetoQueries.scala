package consumers.no_registral.sujeto.application.entity

import consumers.no_registral.sujeto.application.entity.SujetoResponses.{GetAllObnSujetoResponse, GetSujetoResponse}
import design_principles.actor_model.Query

sealed trait SujetoQueries extends Query with SujetoMessage

object SujetoQueries {
  case class GetStateSujeto(sujetoId: String) extends SujetoQueries {
    override type ReturnType = GetSujetoResponse
  }
  case class GetAllObnSujeto(sujetoId: String) extends SujetoQueries {
    override type ReturnType = GetAllObnSujetoResponse
  }
  case class GetSnapshotSujeto(sujetoId: String) extends SujetoQueries {
    override type ReturnType = GetSujetoResponse
  }
}
