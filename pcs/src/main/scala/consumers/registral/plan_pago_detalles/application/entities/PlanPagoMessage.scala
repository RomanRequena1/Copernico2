package consumers.registral.plan_pago_detalles.application.entities

import consumers.registral.plan_pago_detalles.application.entities.PlanPagoMessage.PlanPagoMessageRoots
import design_principles.actor_model.ShardedMessage

trait PlanPagoMessage extends ShardedMessage {

  val planPagoId: String
  val obligacionId: String
  val tipoObjeto: String
  val objetoId: String

  override def aggregateRoot: String =
    PlanPagoMessageRoots(
      planPagoId,
      tipoObjeto,
      objetoId,
      obligacionId,
    ).toString
}

object PlanPagoMessage {

  case class PlanPagoMessageRoots(planPagoId: String, tipoObjeto: String, objetoId: String, obligacionId: String) {
    override def toString = s"Objeto-$objetoId-PlanPago-$planPagoId-TipoObjeto-$tipoObjeto-Obligacion-$obligacionId"
  }
  object PlanPagoMessageRoots {

    def extractor(persistenceId: String): PlanPagoMessageRoots =
      persistenceId match {
        case s"Objeto-$objetoId-PlanPago-$planPagoId-TipoObjeto-$tipoObjeto-Obligacion-$obligacionId" =>
          PlanPagoMessageRoots(planPagoId: String, tipoObjeto: String, objetoId: String, obligacionId: String)
      }
  }
}
