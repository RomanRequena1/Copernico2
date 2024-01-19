package consumers.no_registral.tranferencia.infrastructure.dependency_injection

import akka.actor.Props
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.MonitoringAndMessageProducerTranf
import cqrs.base_actor.untyped.PersistentBaseActor

class TranferenciaActor(requirements: MonitoringAndMessageProducerTranf, objetoActorPropsOption: Option[Props] = None)
  extends PersistentBaseActor[SujetoEvents, SujetoState](requirements.monitoring) {

  var state = SujetoState()


  override def setupHandlers(): Unit = {

    commandBus.subscribe[SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento](new SujetoUpdateFromObjetoTreintaProcientoHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromAnt](new SujetoUpdateFromAntHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromTri](new SujetoUpdateFromTriHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromObjeto](new SujetoUpdateFromObjetoHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoSetBajaFromObjeto](new SujetoSetBajaFromObjetoHandler(this).handle)
    queryBus.subscribe[SujetoQueries.GetStateSujeto](new GetStateSujetoHandler(this).handle)
    queryBus.subscribe[SujetoQueries.GetSnapshotSujeto](new GetSnapshotSujetoHandler(this).handle)
  }

}


object TranferenciaActor extends ShardedEntity[MonitoringAndMessageProducerTranf] {
  def props(tranferenciaActorRequirements: MonitoringAndMessageProducerTranf): Props =
    Props(
      new TranferenciaActor(tranferenciaActorRequirements, None)
    ).withDispatcher("my-dispatcher")
}
