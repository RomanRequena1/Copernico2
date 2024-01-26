package consumers.no_registral.tranferencia.application.entity

import consumers.no_registral.tranferencia.application.entity.TransferenciaResponses.GetTransferenciaResponse
import design_principles.actor_model.Query


sealed trait TransferenciaQueries extends Query with TranferenciaMessage

object TransferenciaQueries {
  case class GetStateTransferencia(objetoId: String) extends TransferenciaQueries {
    override type ReturnType = GetTransferenciaResponse
  }
  case class GetStateExencion(objetoId: String)
    extends TransferenciaQueries {
    override type ReturnType = GetTransferenciaResponse
  }
  case class GetSnapshotTransferencia(objetoId: String) extends TransferenciaQueries {
    override type ReturnType = GetTransferenciaResponse
  }
}