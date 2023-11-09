package consumers.registral.juicio_obn.application.entities

import consumers.registral.juicio_obn.application.entities.JuicioObnResponses.GetJuicioObnResponses
import design_principles.actor_model.Query

sealed trait JuicioObnQueries extends Query with JuicioObnMessage


object JuicioObnQueries {

  case class GetStateJuicioObn(juicioObnId: String,
                               objetoId: String,
                               tipoObjeto: String,
                               obligacionId: String)
    extends JuicioObnQueries
      with Query {
    override type ReturnType = GetJuicioObnResponses
  }
}
