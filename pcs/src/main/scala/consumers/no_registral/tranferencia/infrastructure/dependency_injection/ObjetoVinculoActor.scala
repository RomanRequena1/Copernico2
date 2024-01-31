package consumers.no_registral.tranferencia.infrastructure.dependency_injection

import akka.actor.Props
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.tranferencia.application.cqrs.commands.UpdateObjetoVinculoFromObjHandler
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands
import consumers.no_registral.tranferencia.domain.{ObjetoVinculoEvent, ObjetoVinculoState}
import cqrs.base_actor.untyped.PersistentBaseActor

class ObjetoVinculoActor(requirements: MonitoringAndMessageProducer, objetoActorPropsOption: Option[Props] = None)
  extends PersistentBaseActor[ObjetoVinculoEvent, ObjetoVinculoState](requirements.monitoring) {

  var state: ObjetoVinculoState = ObjetoVinculoState()

  override def setupHandlers(): Unit = {
    commandBus.subscribe[ObjetoVinculoCommands.UpdateVinculoObjetoFromObj](new UpdateObjetoVinculoFromObjHandler(this, requirements).handle)
  }
}
object ObjetoVinculoActor extends ShardedEntity[MonitoringAndMessageProducer] {
  def props(tranferenciaActorRequirements: MonitoringAndMessageProducer): Props =
    Props(
      new ObjetoVinculoActor(tranferenciaActorRequirements, None)
    ).withDispatcher("my-dispatcher")
}
