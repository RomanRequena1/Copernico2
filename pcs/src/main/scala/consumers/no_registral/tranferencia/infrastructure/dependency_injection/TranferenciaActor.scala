package consumers.no_registral.tranferencia.infrastructure.dependency_injection

import akka.actor.Props
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.{MonitoringAndMessageProducer, MonitoringAndMessageProducerTranf}
import consumers.no_registral.tranferencia.application.cqrs.commands.{CreateNewVinculoObjSujToTransfHandler, CreateVinculoObjSujToTransfHandler, UpdateVinculoObjSujToTransfHandler}
import consumers.no_registral.tranferencia.application.entity.TransferenciaCommands
import consumers.no_registral.tranferencia.domain.{TransferenciaEvent, TransferenciaState}
import cqrs.base_actor.untyped.PersistentBaseActor

class TranferenciaActor(requirements: MonitoringAndMessageProducer, objetoActorPropsOption: Option[Props] = None)
  extends PersistentBaseActor[TransferenciaEvent, TransferenciaState](requirements.monitoring) {

  var state: TransferenciaState = TransferenciaState()


  override def setupHandlers(): Unit = {

    commandBus.subscribe[TransferenciaCommands.CreateNewVinculoObjSujToTransf](new CreateNewVinculoObjSujToTransfHandler(this).handle)
    commandBus.subscribe[TransferenciaCommands.CreateVinculoObjSujToTransf](new CreateVinculoObjSujToTransfHandler(this, requirements).handle)
    commandBus.subscribe[TransferenciaCommands.UpdateVinculoObjSujToTransf](new UpdateVinculoObjSujToTransfHandler(this).handle)

  }

}


object TranferenciaActor extends ShardedEntity[MonitoringAndMessageProducer] {
  def props(tranferenciaActorRequirements: MonitoringAndMessageProducer): Props =
    Props(
      new TranferenciaActor(tranferenciaActorRequirements, None)
    ).withDispatcher("my-dispatcher")
}
