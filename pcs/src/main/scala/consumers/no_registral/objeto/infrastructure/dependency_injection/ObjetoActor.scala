package consumers.no_registral.objeto.infrastructure.dependency_injection

import akka.ActorRefMap
import akka.actor.{ActorRef, Props}
import akka.entity.ShardedEntity
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.cqrs.commands._
import consumers.no_registral.objeto.application.cqrs.queries.{GetAllObnObjetoHandler, GetSnapshotObjetoHandler, GetStateExencionHandler, GetStateObjetoHandler}
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetoQueries}
import consumers.no_registral.objeto.domain.ObjetoEvents.{Beneficio, DmnResumenSnapshotPersisted, ObjetoSnapshotPersisted}
import consumers.no_registral.objeto.domain.{ObjetoEvents, ObjetoState}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import consumers.no_registral.obligacion.application.entities.ObligacionCommands._
import consumers.no_registral.obligacion.application.entities.ObligacionMessage._
import consumers.no_registral.obligacion.application.entities.{ObligacionCommands, ObligacionMessage}
import consumers.no_registral.obligacion.domain.ObligacionEvents
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import cqrs.base_actor.untyped.PersistentBaseActor
import io.circe.syntax.EncoderOps
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.MessageProducer

class ObjetoActor(requirements: MonitoringAndMessageProducer,  obligacionActorPropsOption: Option[Props] = None)
    extends PersistentBaseActor[ObjetoEvents, ObjetoState](requirements.monitoring) {
  import ObjetoActor._

  var state = ObjetoState()
  implicit val messageProducer: MessageProducer = requirements.messageProducer

  override def setupHandlers(): Unit = {
    commandBus.subscribe[ObjetoCommands.ObjetoSnapshot](new ObjetoSnapshotHandler(this).handle)
    commandBus.subscribe[ObjetoCommands.UpdateState30ObjetoFromObjVinculo](
      new UpdateState30ObjetoFromObjVinculoHandler(this, requirements).handle
    )
    commandBus.subscribe[ObjetoCommands.ObjetoUpdateFromSujeto](new ObjetoUpdateFromSujetoHandler(this).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoTagAdd](new ObjetoTagAddHandler(this).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoTagRemove](new ObjetoTagRemoveHandler(this).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoUpdateFromAnt](new ObjetoUpdateFromAntHandler(this, requirements).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoUpdateFromTri](new ObjetoUpdateFromTriHandler(this, requirements).handle)
    commandBus.subscribe[ObjetoCommands.RemoveObjetoFromObligacion](
      new ObjetoMapRemoveFromObligacionHandler(this).handle
    )
    commandBus.subscribe[ObjetoCommands.SetBajaObjeto](new SetBajaObjetoHandler(this, requirements).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoUpdateFromObligacion](
      new ObjetoUpdateFromObligacionHandler(this, requirements).handle
    )
    commandBus.subscribe[ObjetoCommands.ObjetoUpdateCotitulares](new ObjetoUpdateCotitularesHandler(this).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoAddExencion](new ObjetoAddExencionHandler(this).handle)
    commandBus.subscribe[ObjetoCommands.ObjetoRemoveObligacion](
      new ObjetoRemoveObligacionHandler(this, requirements).handle
    )
    commandBus.subscribe[ObjetoCommands.ObjetoUpdateFromObnTreintaPorciento](
      new ObjetoUpdateFromObligacionTreintaProcientoHandler(this, requirements).handle
    )
    queryBus.subscribe[ObjetoQueries.GetStateObjeto](new GetStateObjetoHandler(this).handle)
    queryBus.subscribe[ObjetoQueries.GetStateExencion](new GetStateExencionHandler(this).handle)
    queryBus.subscribe[ObjetoQueries.GetSnapshotObjeto](new GetSnapshotObjetoHandler(this).handle)
    queryBus.subscribe[ObjetoQueries.GetAllObnObjeto](new GetAllObnObjetoHandler(this).handle)
  }

  val obligaciones: ObjetoActorRefMap = {
    val obligacionActorProps = obligacionActorPropsOption match {
      case Some(props) => props
      case None => ObligacionActor.props(requirements)
    }
    new ObjetoActorRefMap(
      {
        case (sujetoId, objetoId, tipoObjeto, obligacionId) =>
          val obligacionAggregateRoot = ObligacionMessageRoots(
            sujetoId,
            objetoId,
            tipoObjeto,
            obligacionId
          ).toString()
          context.actorOf(obligacionActorProps, obligacionAggregateRoot)

        case other => context.actorOf(obligacionActorProps, other toString)
      }
    )
  }

  override def receiveCommand: Receive = customReceiveCommand orElse super.receiveCommand
  override def receiveRecover: Receive = customReceiveRecover orElse super.receiveRecover

  def customReceiveCommand: Receive =
    Seq(
      processObligacionMessages
    ) reduce (_ orElse _)

  def customReceiveRecover: Receive = {
    case evt: ObjetoEvents =>
      state += evt
      evt match {
        case evt: ObjetoEvents.ObjetoUpdatedFromObligacion =>
          obligaciones((evt.sujetoId, evt.objetoId, evt.tipoObjeto, evt.obligacionId)) // waking up child
        case _ =>
      }
  }
// TODO: Validar que es lo q esta haciendo, para Objetos ANT?
  def processObligacionMessages: Receive = {
    case childMessage: ObligacionMessage =>
      val obligacion = obligaciones(
        (childMessage.sujetoId, childMessage.objetoId, childMessage.tipoObjeto, childMessage.obligacionId)
      )
      obligacion forward childMessage

      childMessage match {
        case obligacionUpdateFromDto: ObligacionUpdateFromDto =>
          state.exenciones.toSeq.foreach { exencion =>
            obligacion ! ObligacionCommands
              .ObligacionUpdateExencion(
                obligacionUpdateFromDto.deliveryId,
                obligacionUpdateFromDto.sujetoId,
                obligacionUpdateFromDto.objetoId,
                obligacionUpdateFromDto.tipoObjeto,
                obligacionUpdateFromDto.obligacionId,
                exencion
              )
          }
        case _ => ()
      }
  }

  def persistSnapshot(evt: ObjetoEvents, consolidatedState: ObjetoState)(handler: () => Unit): Unit = {
    val kafkaTopic = "ObjetoSnapshotPersistedReadside"
    val snapshot =
      ObjetoSnapshotPersisted(
        evt.deliveryId,
        evt.sujetoId,
        evt.objetoId,
        objetoId2 = evt match {
          case evt: ObjetoEvents.ObjetoUpdatedFromObligacion => evt.objetoId2
          case _ => None
        },
        evt.tipoObjeto,
        consolidatedState.saldo,
        consolidatedState.sujetos,
        consolidatedState.tags,
        consolidatedState.sujetoResponsable,
        consolidatedState.porcentajeResponsabilidad,
        consolidatedState.registro,
        consolidatedState.obligacionesSaldo,
        consolidatedState.cuotas,
        consolidatedState.clasificacionObjeto,
        operacion = ObligacionEvents.operaciones.get("Upsert").get,
        idExterno = evt match {
          case evt: ObjetoEvents.ObjetoUpdatedFromObligacion => evt.idExterno
          case _ => None
        },
        Some(consolidatedState.tiene30Objeto),
        consolidatedState.aplicarDescuento,
        consolidatedState.resulDmn.getOrElse(0),
        consolidatedState.exclusionObjeto,
        Some(consolidatedState.tiene30ObjetoVinculo)
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

  def dmnresumenpersistSnapshot(evt: ObjetoEvents, consolidatedState: ObjetoState)(handler: () => Unit): Unit = {
    val kafkaTopic = "dgr-cop-objeto-beneficios-v1"

    val beneficio = Beneficio(
      codigo = "DTO30",
      aplicarDescuento = consolidatedState.aplicarDescuento,
      dmnNumero = consolidatedState.dmnNumero,
      dmnDescripcion = consolidatedState.dmnDescripcion
    )
    val snapshot =
      DmnResumenSnapshotPersisted(
        evt.deliveryId,
        evt.sujetoId,
        evt.objetoId,
        evt.tipoObjeto,
        consolidatedState.registro.flatMap(_.SOJ_ID_EXTERNO).orElse(Some("None")),
        Some(consolidatedState.fechaUltMod),
        beneficios = Seq(beneficio)
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
  def deleteSnapshot(evt: ObjetoEvents, consolidatedState: ObjetoState)(handler: () => Unit): Unit = {
    val kafkaTopic = "ObjetoSnapshotPersistedReadside"
    val snapshot =
      ObjetoSnapshotPersisted(
        evt.deliveryId,
        evt.sujetoId,
        evt.objetoId,
        objetoId2 = evt match {
          case evt: ObjetoEvents.ObjetoUpdatedFromObligacion => evt.objetoId2
          case _ => None
        },
        evt.tipoObjeto,
        consolidatedState.saldo,
        consolidatedState.sujetos,
        consolidatedState.tags,
        consolidatedState.sujetoResponsable,
        consolidatedState.porcentajeResponsabilidad,
        consolidatedState.registro,
        consolidatedState.obligacionesSaldo,
        consolidatedState.cuotas,
        consolidatedState.clasificacionObjeto,
        operacion = ObligacionEvents.operaciones.get("Delete").get,
        idExterno = evt match {
          case evt: ObjetoEvents.ObjetoUpdatedFromObligacion => evt.idExterno
          case _ => None
        },
        Some(consolidatedState.tiene30Objeto),
        consolidatedState.aplicarDescuento,
        consolidatedState.resulDmn.getOrElse(0),
        consolidatedState.exclusionObjeto,
        Some(consolidatedState.tiene30ObjetoVinculo)
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
  def deleteObjetoObligacionesSnapshot(evt: ObjetoEvents, consolidatedState: ObjetoState)(handler: () => Unit): Unit = {
    val kafkaTopic = "ObjetoSnapshotPersistedReadside"
    val snapshot =
      ObjetoSnapshotPersisted(
        evt.deliveryId,
        evt.sujetoId,
        evt.objetoId,
        objetoId2 = evt match {
          case evt: ObjetoEvents.ObjetoUpdatedFromObligacion => evt.objetoId2
          case _ => None
        },
        evt.tipoObjeto,
        consolidatedState.saldo,
        consolidatedState.sujetos,
        consolidatedState.tags,
        consolidatedState.sujetoResponsable,
        consolidatedState.porcentajeResponsabilidad,
        consolidatedState.registro,
        consolidatedState.obligacionesSaldo,
        consolidatedState.cuotas,
        consolidatedState.clasificacionObjeto,
        operacion = ObligacionEvents.operaciones.get("FullDelete").get,
        idExterno = evt match {
          case evt: ObjetoEvents.ObjetoUpdatedFromObligacion => evt.idExterno
          case _ => None
        },
        Some(consolidatedState.tiene30Objeto),
        consolidatedState.aplicarDescuento,
        consolidatedState.resulDmn.getOrElse(0),
        consolidatedState.exclusionObjeto,
        Some(consolidatedState.tiene30ObjetoVinculo)
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

  /*def removeObligaciones(): Unit =
    state.obligaciones.foreach { obligacionId =>
      val aggregateRoots = ObjetoMessageRoots.extractor(persistenceId)
      val obligacion =
        obligaciones((aggregateRoots.sujetoId, aggregateRoots.objetoId, aggregateRoots.tipoObjeto, obligacionId))
      obligacion ! ObligacionCommands.ObligacionRemove(aggregateRoots.sujetoId,
                                                       aggregateRoots.objetoId,
                                                       aggregateRoots.tipoObjeto,
                                                       obligacionId)
    }

  def withCotitulares(sujetos: Set[String]): Boolean =
    sujetos.size > 1*/

  def informParentTreintaPorciento(deliveryId: BigInt,
                                   sujetoId: String,
                                   objetoId: String,
                                   tipoObjeto: String,
                                   state: ObjetoState): Unit = {
    context.parent ! SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento(
      deliveryId,
      sujetoId,
      objetoId,
      tipoObjeto,
      state.saldo,
      state.obligacionesSaldo.values.sum,
      state.clasificacionObjeto
    )
  }

  def informParent(deliveryId: BigInt,
                   sujetoId: String,
                   objetoId: String,
                   tipoObjeto: String,
                   state: ObjetoState): Unit = {
    context.parent ! SujetoCommands.SujetoUpdateFromObjeto(
      deliveryId, //este?
      sujetoId,
      objetoId,
      tipoObjeto,
      state.saldo,
      state.obligacionesSaldo.values.sum,
      state.clasificacionObjeto
    )
  }

  def informParentAnt(deliveryId: BigInt,
                      sujetoId: String,
                      objetoId: String,
                      tipoObjeto: String,
                      state: ObjetoState): Unit = {
    context.parent ! SujetoCommands.SujetoUpdateFromObjetoAnt(
      deliveryId,
      sujetoId,
      objetoId,
      tipoObjeto,
      state.saldo,
      state.obligacionesSaldo.values.sum,
      state.clasificacionObjeto
    )
  }

  def informBajaToParent(cmd: ObjetoCommands): Unit = {
    context.parent ! SujetoCommands.SujetoSetBajaFromObjeto(
      cmd.deliveryId,
      cmd.sujetoId,
      cmd.objetoId,
      cmd.tipoObjeto
    )
  }
}

object ObjetoActor extends ShardedEntity[MonitoringAndMessageProducer] {
  def props(requirements: MonitoringAndMessageProducer): Props =
    Props(new ObjetoActor(requirements, None)).withDispatcher("my-dispatcher") //TODO added my-dispatcher
  type ObligacionAgregateRoot = (String, String, String, String)
  class ObjetoActorRefMap(newActor: ObligacionAgregateRoot => ActorRef)
      extends ActorRefMap[ObligacionAgregateRoot](newActor)
}
