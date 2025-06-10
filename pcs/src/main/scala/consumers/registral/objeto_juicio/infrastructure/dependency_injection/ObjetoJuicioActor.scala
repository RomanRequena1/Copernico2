package consumers.registral.objeto_juicio.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.objeto_juicio.application.cqrs.commands.{ObjetoJuicioRemoveFromDtoHandler, ObjetoJuicioUpdateFromDtoHandler}
import consumers.registral.objeto_juicio.application.cqrs.queries.GetStateObjetoJuicioHandler
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioCommands.{ObjetoJuicioUpdateFromDto, RemoveObjetoJuicioFromDto}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioMessage
import akka.projections.ProjectionSettings
import akka.cluster.sharding.{ClusterSharding => ClassicClusterSharding}
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioQueries.GetStateObjetoJuicio
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.{ObjetoJuicioRemovedFromDto, ObjetoJuicioUpdatedFromDto}
import consumers.registral.objeto_juicio.domain.events.{ObjetoJuicioRemovedFromDtoHandler, ObjetoJuicioUpdatedFromDtoHandler}
import consumers.registral.objeto_juicio.domain.{ObjetoJuicioEvents, ObjetoJuicioState}
import consumers.registral.objeto_juicio.infrastructure.projection.ObjetoJuicioProjectionHandler
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer
import monitoring.Monitoring


case class ObjetoJuicioActor(state: ObjetoJuicioState = ObjetoJuicioState(), projFlag: Boolean)(
    implicit
    messageProducer: MessageProducer,
    system: ActorSystem[Nothing],
    monitoring: Monitoring
) extends BasePersistentShardedTypedActorWithCQRS[
      ObjetoJuicioMessage,
      ObjetoJuicioEvents,
      ObjetoJuicioState
    ](state) {

  commandBus.subscribe[ObjetoJuicioUpdateFromDto](new ObjetoJuicioUpdateFromDtoHandler().handle)
  commandBus.subscribe[RemoveObjetoJuicioFromDto](new ObjetoJuicioRemoveFromDtoHandler().handle)
  queryBus.subscribe[GetStateObjetoJuicio](new GetStateObjetoJuicioHandler(this).handle)
  eventBus.subscribe[ObjetoJuicioUpdatedFromDto](new ObjetoJuicioUpdatedFromDtoHandler().handle)
  eventBus.subscribe[ObjetoJuicioRemovedFromDto](new ObjetoJuicioRemovedFromDtoHandler().handle)


  override def getTags(event: ObjetoJuicioEvents): Set[String] = {
    event match {
      case _: ObjetoJuicioUpdatedFromDto => Set("ObjetoJuicio-updated")
      case _ => Set("ObjetoJuicio-updated")
    }
  }

  if (projFlag) {
    val projSettings1: ProjectionSettings = ProjectionSettings.default("ObjetoJuicio-updated", 1)(this.monitoring)
    val projHandler1 = new ObjetoJuicioProjectionHandler(projSettings1, this.system, this)
    println(s"CHECK ${projSettings1.toString}")
    projHandler1.run()
  }
}
