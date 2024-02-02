package consumers.no_registral.sujeto.infrastructure.dependency_injection

import akka.ActorRefMap
import akka.actor.{ActorRef, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.{MonitoringAndMessageProducer, MonitoringAndMessageProducerTranf}
import consumers.no_registral.objeto.application.entities.ObjetoMessage
import consumers.no_registral.objeto.application.entities.ObjetoMessage.ObjetoMessageRoots
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.application.entities.ObligacionMessage
import consumers.no_registral.sujeto.application.cqrs.commands.{SujetoSetBajaFromObjetoHandler, SujetoUpdateFromAntHandler, SujetoUpdateFromObjetoHandler, SujetoUpdateFromObjetoTreintaProcientoHandler, SujetoUpdateFromTriHandler}
import consumers.no_registral.sujeto.application.cqrs.queries.{GetSnapshotSujetoHandler, GetStateSujetoHandler}
import consumers.no_registral.sujeto.application.entity.SujetoMessage.SujetoMessageRoots
import consumers.no_registral.sujeto.application.entity.{SujetoCommands, SujetoQueries}
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import consumers.no_registral.sujeto.domain.{SujetoEvents, SujetoState}
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor.SujetoActorRefMap
import cqrs.base_actor.untyped.PersistentBaseActor
import kafka.KafkaMessageProducer.KafkaKeyValue
import io.circe.syntax.EncoderOps
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits._
import design_principles.microservice.kafka_consumer_microservice.KafkaConsumerMicroserviceRequirements


class SujetoActor(requirements: MonitoringAndMessageProducer, objetoActorPropsOption: Option[Props] = None)
    extends PersistentBaseActor[SujetoEvents, SujetoState](requirements.monitoring) {

  var state = SujetoState()

  val objetos: SujetoActorRefMap = {
    val objetoActorProps = objetoActorPropsOption match {
      case Some(props) => props
      case None => ObjetoActor.props(requirements)
    }
    new SujetoActorRefMap(
      {
        case (sujetoId, objetoId, tipoObjeto) =>
          val objetoAggregateRoot = ObjetoMessageRoots(sujetoId, objetoId, tipoObjeto).toString
          context.actorOf(objetoActorProps, objetoAggregateRoot)
        case other =>
          context.actorOf(objetoActorProps, other toString)
      }
    )
  }

  override def setupHandlers(): Unit = {
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento](new SujetoUpdateFromObjetoTreintaProcientoHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromAnt](new SujetoUpdateFromAntHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromTri](new SujetoUpdateFromTriHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoUpdateFromObjeto](new SujetoUpdateFromObjetoHandler(this).handle)
    commandBus.subscribe[SujetoCommands.SujetoSetBajaFromObjeto](new SujetoSetBajaFromObjetoHandler(this).handle)
    queryBus.subscribe[SujetoQueries.GetStateSujeto](new GetStateSujetoHandler(this).handle)
    queryBus.subscribe[SujetoQueries.GetSnapshotSujeto](new GetSnapshotSujetoHandler(this).handle)
  }

  override def receiveCommand: Receive = customReceiveCommand orElse super.receiveCommand

  def customReceiveCommand: Receive = {
    case childMessage: ObligacionMessage =>
      objetos((childMessage.sujetoId, childMessage.objetoId, childMessage.tipoObjeto)) forward childMessage
    case childMessage: ObjetoMessage =>
      objetos((childMessage.sujetoId, childMessage.objetoId, childMessage.tipoObjeto)) forward childMessage
  }

  override def receiveRecover: Receive = customReceiveRecover orElse super.receiveRecover
  def customReceiveRecover: Receive = {
    case evt: SujetoEvents.SujetoUpdatedFromObjeto =>
      state += evt
      objetos((evt.sujetoId, evt.objetoId, evt.tipoObjeto))
  }

  import consumers.no_registral.sujeto.infrastructure.json._
  def persistSnapshot()(handler: Seq[KafkaKeyValue] => Unit): Unit = {
    val sujetoId = SujetoMessageRoots.extractor(persistenceId).sujetoId
    val event = SujetoSnapshotPersisted(state.registro.map(_.EV_ID).getOrElse(state.lastInternalDeliveryId),
      sujetoId,
      state.registro,
      state.saldo,
      Some(state.tiene30Sujeto)).asJson.toString()
    requirements.messageProducer.produce(
      data = Seq(KafkaKeyValue(persistenceId, event)),
      topic = "SujetoSnapshotPersisted"
    )(handler)
  }
}

object SujetoActor extends ShardedEntity[MonitoringAndMessageProducer] {
  def props(sujetoActorRequirements: MonitoringAndMessageProducer): Props =
    Props(
      new SujetoActor(sujetoActorRequirements, None)
    ).withDispatcher("my-dispatcher")
  type ObjetoAggregateRoot = (String, String, String)
  class SujetoActorRefMap(newActor: ObjetoAggregateRoot => ActorRef) extends ActorRefMap[ObjetoAggregateRoot](newActor)

}
