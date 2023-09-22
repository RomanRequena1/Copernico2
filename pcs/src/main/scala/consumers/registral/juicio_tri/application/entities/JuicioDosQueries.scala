package consumers.registral.juicio_tri.application.entities

import consumers.registral.juicio_tri.application.entities.JuicioDosResponses.GetJuicioDosResponse
import design_principles.actor_model.Query

sealed trait JuicioDosQueries extends Query with JuicioDosMessage
  object JuicioDosQueries {
    case class GetStateJuicioDos(
                               juicioId: String
                             ) extends JuicioDosQueries {
      override type ReturnType = GetJuicioDosResponse
    }

}
