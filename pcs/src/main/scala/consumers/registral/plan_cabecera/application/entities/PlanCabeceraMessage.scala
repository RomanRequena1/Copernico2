package consumers.registral.plan_cabecera.application.entities

import consumers.registral.plan_cabecera.application.entities.PlanCabeceraMessage.PlanCabeceraMessageRoots
import design_principles.actor_model.ShardedMessage

trait PlanCabeceraMessage extends ShardedMessage{

  val planCabeceraId: String

  override def aggregateRoot: String =
    PlanCabeceraMessageRoots(
      planCabeceraId,
    ).toString
}

object PlanCabeceraMessage{

  case class PlanCabeceraMessageRoots(planCabeceraId: String){
    override def toString = s"-PlanCabecera-$planCabeceraId"
  }
  object PlanCabeceraMessageRoots {

    def extractor(persistenceId: String): PlanCabeceraMessageRoots =
      persistenceId match {
        case s"-PlanCabecera-$planCabeceraId" =>
          PlanCabeceraMessageRoots(planCabeceraId: String)
      }
  }
}
