package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.helper.{SendObjetoToObjetoVinculo, SendToObligaciones}
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class SetBajaObjetoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.SetBajaObjeto] {
  override def handle(
                       command: ObjetoCommands.SetBajaObjeto
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    println(s"[HANDLER-BAJA-ENTRY] deliveryId=${command.deliveryId}, sujetoId=${command.sujetoId}, " +
      s"objetoId=${command.objetoId}, estado=${command.registro.SOJ_ESTADO}")

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val event = ObjetoEvents.ObjetoBajaSet(
      actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable
    )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      println(s"[HANDLER-BAJA-IDEMPOTENT] deliveryId=${command.deliveryId}, objetoId=${command.objetoId}")
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)

    } else {

      implicit val ac: ActorSystem = actor.context.system
      val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

      actor.persistEvent(event) { () =>
        println(s"[HANDLER-BAJA-PERSISTED] deliveryId=${command.deliveryId}, objetoId=${command.objetoId}")
        actor.state += event
        actor.informBajaToParent(command)
        actor.deleteSnapshot(event, actor.state) { () =>
          actor.deleteObjetoObligacionesSnapshot(event, actor.state) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }
        SendToObligaciones(actor)
        println(s"[HANDLER-BAJA-SENDING] deliveryId=${command.deliveryId}, objetoId=${command.objetoId} - " +
          s"Llamando SendObjetoToObjetoVinculo")
        SendObjetoToObjetoVinculo(Obje,
          actor,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.registro.SOJ_ESTADO,
          requeriment,
          command)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}