package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.domain.ObjetoEvents.UpdatedState30ObjetoFromObjVinculo
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import design_principles.actor_model.Response

object SendToSujeto1 {

  def apply(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer, event: UpdatedState30ObjetoFromObjVinculo, deliveryIdObligacion: Option[BigInt], idExterno: Option[String]): Unit = {
    implicit val system: ActorSystem = actor.context.system
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(requeriment)
    actorSujetoGeneral.ask[Response.SuccessProcessing](SujetoCommands.SujetoUpdateFromObjeto(
      event.deliveryId,
      event.sujetoId,
      event.objetoId,
      event.tipoObjeto,
      actor.state.saldo,
      actor.state.obligacionesSaldo.values.sum,
      actor.state.clasificacionObjeto,
      idExterno,
      deliveryIdObligacion
    ))
    actor.context.sender ! Response.SuccessProcessing(event.aggregateRoot, event.deliveryId)
  }
}
