package consumers.registral.componente_i.application.entities

import consumers.registral.componente_i.application.entities.ComponenteIResponses.GetComponenteIResponse
import design_principles.actor_model.Query

sealed trait ComponenteIQueries extends Query with ComponenteIMessage


object ComponenteIQueries {
  case class GetStateComponenteI(
                             sujetoId: String,
                             objetoId: String,
                             tipoObjeto: String,
                             obligacionId: String
                                   ) extends ComponenteIQueries {
    override type ReturnType = GetComponenteIResponse


  }
}
