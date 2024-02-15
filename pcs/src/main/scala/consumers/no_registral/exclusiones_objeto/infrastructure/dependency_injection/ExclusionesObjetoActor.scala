package consumers.no_registral.exclusiones_objeto.infrastructure.dependency_injection

import akka.actor.Props
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.exclusiones_objeto.application.cqrs.commands.ExclusionesObjetoUpdateFromDtoHandler
import consumers.no_registral.exclusiones_objeto.application.cqrs.queries.GetStateExclusionesObjetoHandler
import consumers.no_registral.exclusiones_objeto.application.entities.{ExclusionesObjetoCommands, ExclusionesObjetoQueries}
import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
import consumers.no_registral.exclusiones_objeto.domain.{ExclusionesObjetoEvents, ExclusionesObjetoState}
import cqrs.base_actor.untyped.PersistentBaseActor
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer
import io.circe.syntax.EncoderOps
import consumers.no_registral.exclusiones_objeto.infrastructure.json.ExclusionesObjetoImplicits._
class ExclusionesObjetoActor (requirements: MonitoringAndMessageProducer, exclusionesObjetoPropsOption: Option[Props] = None)
  extends PersistentBaseActor[ExclusionesObjetoEvents,ExclusionesObjetoState](requirements.monitoring) {
  import ExclusionesObjetoActor._
  var state = ExclusionesObjetoState()
  implicit val messageProducer: MessageProducer = requirements.messageProducer

  override def setupHandlers(): Unit = {
    commandBus.subscribe[ExclusionesObjetoCommands.ExclusionesObjetoUpdateFromDto](new ExclusionesObjetoUpdateFromDtoHandler(this, requirements).handle)
    queryBus.subscribe[ExclusionesObjetoQueries.GetStateExclusionesObjeto](new GetStateExclusionesObjetoHandler(this).handle)

  }

  def persistSnapshot(evt: ExclusionesObjetoEvents, consolidatedState: ExclusionesObjetoState)(handler: () => Unit): Unit = {
    val kafkaTopic = "ExclusionesObjetoPersistedReadside"
    val snapshot =
      ExclusionesObjetoUpdatedFromDto(
        consolidatedState.lastDeliveryIdByEvents,
        consolidatedState.objetoId,
        consolidatedState.registro.get
      )
    requirements.messageProducer.produce(
      data = Seq(
        KafkaKeyValue(
          snapshot.aggregateRoot,
          snapshot.asJson.toString()
        )
      ),
      topic = kafkaTopic
    ) { _ =>
      handler()
    }

  }
}
object ExclusionesObjetoActor extends ShardedEntity[MonitoringAndMessageProducer] {
  def props(exclusionesObjetoActorRequirements: MonitoringAndMessageProducer): Props =
    Props(
      new ExclusionesObjetoActor(exclusionesObjetoActorRequirements, None)
    ).withDispatcher("my-dispatcher")

}