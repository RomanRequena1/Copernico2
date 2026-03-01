package consumers.no_registral.tranferencia.infrastructure.dependency_injection

import akka.actor.Props
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.domain.ObjetoEvents.{Beneficio, DmnResumen, DmnResumenSnapshotPersisted}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import consumers.no_registral.tranferencia.application.cqrs.commands.{AuditoriaHandler, CreateVinculoObjetoFromObjTranfHandler, RemoveObjetoVinculoFromObjHandler, UpdateObjetoVinculoFromObjHandler}
import consumers.no_registral.tranferencia.application.cqrs.queries.GetStateObjetoVinculoHandler
import consumers.no_registral.tranferencia.application.entity.{ObjetoVinculoCommands, ObjetoVinculoQueries}
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent.ObjetoVinculoSnapshotPersisted
import consumers.no_registral.tranferencia.domain.{ObjetoVinculoEvent, ObjetoVinculoState}
import consumers.no_registral.tranferencia.infrastructure.json.ObjetoVinculoImplicits.ObjetoVinculoSnapshotPersistedEncoder
import cqrs.base_actor.untyped.PersistentBaseActor
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer


class ObjetoVinculoActor(requirements: MonitoringAndMessageProducer, objetoVinculoPropsOption: Option[Props] = None)
  extends PersistentBaseActor[ObjetoVinculoEvent, ObjetoVinculoState](requirements.monitoring) {

  var state: ObjetoVinculoState = ObjetoVinculoState()
  implicit val messageProducer: MessageProducer = requirements.messageProducer


  override val persistenceId: String = {
    val entityId = self.path.name
    s"ObjetoVinculo-$entityId"
  }

  override def setupHandlers(): Unit = {
    commandBus.subscribe[ObjetoVinculoCommands.UpdateVinculoObjetoFromObj](new UpdateObjetoVinculoFromObjHandler(this, requirements).handle)
    commandBus.subscribe[ObjetoVinculoCommands.CreateTransfVinculoObjetoFromObj](new CreateVinculoObjetoFromObjTranfHandler(this, requirements).handle)
    commandBus.subscribe[ObjetoVinculoCommands.RemoveObjetoVinculo](new RemoveObjetoVinculoFromObjHandler(this, requirements).handle)
    commandBus.subscribe[ObjetoVinculoCommands.AuditarYEnviarResumen](new AuditoriaHandler(this, requirements).handle)
    queryBus.subscribe[ObjetoVinculoQueries.GetStateObjetoVinculo](new GetStateObjetoVinculoHandler(this).handle)
  }

  def persistSnapshot(evt: ObjetoVinculoEvent, consolidatedState: ObjetoVinculoState)(handler: () => Unit): Unit = {
    val kafkaTopic = "ObjetoVinculoPersisted"
    val snapshot =
      ObjetoVinculoSnapshotPersisted(
        evt.objetoId,
        evt.tipoObj,
        consolidatedState.tiene30ObjetoVinculo,
        consolidatedState.mapTransf,
        consolidatedState.mapVinculo,
        consolidatedState.exclusionObjetoVinculo,
        consolidatedState.lastDeliveryIdByEvents)

    requirements.messageProducer.produce(
      data = Seq(
        KafkaKeyValue(
          snapshot.aggregateRoot,
          snapshot.asJson.noSpaces
        )
      ),
      topic = kafkaTopic
    ) { _ =>
      handler()
    }
  }

  // En ObjetoVinculoActor.scala

  def dmnresumenpersistSnapshot(evt: DmnResumen, consolidatedState:  ObjetoVinculoState, idExterno: Option[String])(handler: () => Unit): Unit = {
    val kafkaTopic = "dgr-cop-objeto-beneficios-v1"

    val beneficio = Beneficio(
      codigo = "DTO30",
      aplicarDescuento = evt.aplicarDescuento,
      dmnNumero = evt. dmnNumero,
      dmnDescripcion = evt. dmnDescripcion
    )

    val snapshot = DmnResumenSnapshotPersisted(
      evt.deliveryId,
      evt.sujetoId,
      evt.objetoId,
      evt.tipoObjeto,
      idExterno,
      evt.fecha,
      Seq(beneficio)
    )

    requirements.psrmMessageProducer.produce(
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

object ObjetoVinculoActor extends ShardedEntity[MonitoringAndMessageProducer] {
  def props(tranferenciaActorRequirements: MonitoringAndMessageProducer): Props =
    Props(
      new ObjetoVinculoActor(tranferenciaActorRequirements, None)
    ).withDispatcher("my-dispatcher")

}
