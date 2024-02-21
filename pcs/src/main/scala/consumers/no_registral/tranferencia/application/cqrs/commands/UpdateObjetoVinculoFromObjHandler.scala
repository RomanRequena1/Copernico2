package consumers.no_registral.tranferencia.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.UpdateState30ObjetoFromObjVinculo
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.UpdateVinculoObjetoFromObj
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import scala.util.{Success, Try}


class UpdateObjetoVinculoFromObjHandler(actor: ObjetoVinculoActor, tranferenciaActorRequirements: MonitoringAndMessageProducer) extends SyncCommandHandler[UpdateVinculoObjetoFromObj] {
  override def handle(command: UpdateVinculoObjetoFromObj): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    log.error("Llego? CreateVinculoObjSujToTransfHandler::::: " + command + " - "
      + command.tiene30Objeto + " - "
      + command.objetoId + " - "
      + command.sujetoId)

    val event = ObjetoVinculoEvent.UpdatedVinculoObjetoFromObj(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30Objeto,
      command.isResponsable,
      command.estadoObj,
      command.titularidad
    )

    log.error("EVENTO DE UPDATEVINCULOFROMOBJ: "+event)
    println("STATE ::::::::::: " + actor.state.mapVinculo)
    implicit val ssytem: ActorSystem = actor.context.system
    //todo para mandar mensajes a todos los objetos de los distintos vinculos
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)

    actor.persistEvent(event) { () =>

      actor.state += event
      log.error("mapViculo -> " + actor.state.mapVinculo)
      actor.state.mapVinculo.foreach {
        e => {
          log.error("vin -> " + e._1)
            actorSujetoGeneral ! UpdateState30ObjetoFromObjVinculo(0, e._1.sujetoId, e._1.objetoId, e._1.tipoObj, actor.state.tiene30ObjetoVinculo)
        }
      }
      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

// objeto -> transf -> objeto

// tranf -> objeto -> tranf