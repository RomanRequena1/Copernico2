package consumers.no_registral.obligacion.infrastructure.dependency_injection

import akka.actor.{ActorSelection, Props}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.obligacion.application.cqrs.commands._
import consumers.no_registral.obligacion.application.cqrs.queries.{ObligacionGetStateHandler, ObligacionSnapshotHandler}
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemove
import consumers.no_registral.obligacion.application.entities.ObligacionMessage.ObligacionMessageRoots
import consumers.no_registral.obligacion.application.entities.{ObligacionCommands, ObligacionQueries}
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.domain.{ObligacionEvents, ObligacionState}
import cqrs.base_actor.untyped.PersistentBaseActor
import kafka.KafkaMessageProducer.KafkaKeyValue
import timescaledb.TimescaledbPcsToKafka.connOracleWriteSideToKafka

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


class ObligacionActor(requirements: MonitoringAndMessageProducer)
    extends PersistentBaseActor[ObligacionEvents, ObligacionState](requirements.monitoring) {
  //val timescaledbActorSelector: ActorSelection = context.actorSelection("akka://PersonClassificationService/user/timescaledb")
  val enable = Try(System.getenv("ENABLE_TRAZ")).getOrElse("no")

  var state = ObligacionState()

  override def setupHandlers(): Unit = {
    queryBus.subscribe[ObligacionQueries.GetStateObligacion](new ObligacionGetStateHandler(this).handle)
    queryBus.subscribe[ObligacionQueries.GetSnapshotObligacion](new ObligacionSnapshotHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionUpdateFromDto](new ObligacionUpdateFromDtoHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionUpdateExencion](new ObligacionUpdateExencionHandler(this).handle)
    commandBus.subscribe[ObligacionCommands.ObligacionRemove](new ObligacionRemoveHandler(this).handle)
  }

  def informParent(cmd: ObligacionCommands): Unit = {
    context.parent ! ObjetoCommands.ObjetoUpdateFromObligacion(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd match {
        case c: ObligacionCommands.ObligacionUpdateFromDto => c.registro.BOB_SOJ_IDENTIFICADOR_2 match {
          case Some(value) => Some(value)
          case None => None
        }
        case _ => None
      },
      cmd.tipoObjeto,
      cmd.obligacionId,
      state.saldo,
      state.exenta,
      state.porcentajeExencion,
      state.idExterno
    )
  }


  def informRemoveToParent(cmd: ObligacionRemove): Unit = {
    context.parent ! ObjetoCommands.ObjetoRemoveObligacion(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto,
      cmd.obligacionId,
      cmd.cuota,
    )
  }

  import consumers.no_registral.obligacion.infrastructure.json._
  def persistSnapshot()(handler: () => Unit): Unit = {
    val ids = ObligacionMessageRoots.extractor(persistenceId)

    val kafkaTopic = "ObligacionPersistedSnapshot"
    //logger.error("V2 = " + state.registro.get.BOB_VENCIMIENTO_2.getOrElse("no esta"))
    val event = ObligacionPersistedSnapshot(
      deliveryId = lastDeliveryId,
      sujetoId = ids.sujetoId,
      objetoId = ids.objetoId,
      tipoObjeto = ids.tipoObjeto,
      obligacionId = ids.obligacionId,
      registro = state.registro,
      exenta = state.exenta,
      porcentajeExencion = state.porcentajeExencion.getOrElse(0),
      saldo = state.saldo,
      operacion = ObligacionEvents.operaciones.get("Upsert").get
    )
    import serialization.encode
    requirements.messageProducer.produce(
      data = Seq(
        KafkaKeyValue(
          persistenceId,
          encode(event)
        )
      ),
      topic = kafkaTopic
    )(_ => handler()).onComplete {
      case Failure(ex) => log.error("Error when try to send to topic " + ex)
      case Success(value) => {
        log.debug("Success,  sent to topic")
        //println("CUMBIA actor " + timescaledbActorSelector)
        //timescaledbActorSelector ! InsertFromActor(event.deliveryId.toString(), timescaledbActorSelector)
        //if (enable.equals("true")) {
          //Future(connOracleWriteSideToKafka(event.deliveryId.toString()))
        //}
      }
    }
  }

  def deleteSnapshot()(handler: () => Unit): Unit = {
    val ids = ObligacionMessageRoots.extractor(persistenceId)

    val kafkaTopic = "ObligacionPersistedSnapshot"

    val event = ObligacionPersistedSnapshot(
      deliveryId = lastDeliveryId,
      sujetoId = ids.sujetoId,
      objetoId = ids.objetoId,
      tipoObjeto = ids.tipoObjeto,
      obligacionId = ids.obligacionId,
      registro = state.registro,
      exenta = state.exenta,
      porcentajeExencion = state.porcentajeExencion.getOrElse(0),
      saldo = state.saldo,
      operacion = ObligacionEvents.operaciones.get("Delete").get
    )
    import serialization.encode
    requirements.messageProducer.produce(
      data = Seq(
        KafkaKeyValue(
          persistenceId,
          encode(event)
        )
      ),
      topic = kafkaTopic
    )(_ => handler()).onComplete {
      case Failure(ex) => log.error("Error when try to send to topic " + ex)
      case Success(value) => {
        log.debug("Success,  sent to topic")
        //timescaledbActorSelector ! InsertFromActor(event.deliveryId.toString(), timescaledbActorSelector)
        //if (enable.equals("true")) {
          //Future(connOracleWriteSideToKafka(event.deliveryId.toString()))
        //}
      }
    }
  }
}

object ObligacionActor {
  def props(requirements: MonitoringAndMessageProducer): Props =
    Props(new ObligacionActor(requirements)).withDispatcher("my-dispatcher") //TODO added my-dispatcher
}
