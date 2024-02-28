package consumers.no_registral.obligacion.application.helper

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.domain.ObjetoEvents.RemovedObjetoFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionRemovedInfoFromObjeto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor

object SendObligacionToObjeto {

  def apply(actor: ObligacionActor, requeriment: MonitoringAndMessageProducer, event: ObligacionRemovedInfoFromObjeto) : Unit = {

    implicit val system: ActorSystem = actor.context.system
    implicit val actorObjetoGeneral: ActorRef = ObjetoActor.startWithRequirements(requeriment)

    actorObjetoGeneral ! RemovedObjetoFromObligacion(
      event.deliveryId,
      event.sujetoId,
      event.objetoId,
      event.tipoObjeto,
      event.obligacionId,
      event.cuota
    )
  }
}
