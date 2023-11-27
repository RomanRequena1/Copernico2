package consumers.registral.exclusiones_sujeto.application.entities

import consumers.registral.exclusiones_sujeto.application.entities.ExclusionesSujetoResponses.GetExclusionesSujetoResponse
import design_principles.actor_model.Query

sealed trait ExclusionesSujetoQueries extends Query with ExclusionesSujetoMessage

object ExclusionesSujetoQueries {
  case class GetStateExclusionesSujeto(
                                       sujetoId: String
                                     ) extends ExclusionesSujetoQueries {
    override type ReturnType = GetExclusionesSujetoResponse
  }
}
