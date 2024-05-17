package consumers.no_registral.obligacion.application.helper

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.RemoveObjetoFromObligacion
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionRemovedInfoFromObjeto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor

object SendObligacionToObjeto {

  def apply(actor: ObligacionActor, requeriment: MonitoringAndMessageProducer, event: ObligacionRemovedInfoFromObjeto) : Unit = {

    implicit val system: ActorSystem = actor.context.system
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(requeriment)


    actorSujetoGeneral ! RemoveObjetoFromObligacion(
      event.deliveryId,
      event.sujetoId,
      event.objetoId,
      event.tipoObjeto,
      event.obligacionId,
      event.cuota
    )
  }
}
