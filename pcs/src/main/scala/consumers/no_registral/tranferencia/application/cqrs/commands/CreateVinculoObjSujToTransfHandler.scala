package consumers.no_registral.tranferencia.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.CommandTest
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.CreateObjetoVinculoFromObj
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class CreateVinculoObjSujToTransfHandler(actor: ObjetoVinculoActor, tranferenciaActorRequirements: MonitoringAndMessageProducer) extends SyncCommandHandler[CreateObjetoVinculoFromObj] {
  override def handle(command: CreateObjetoVinculoFromObj): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    println("Llego? CreateVinculoObjSujToTransfHandler::::: " + command + " - "
      + command.tiene30Objeto + " - "
      + actor.context.self.path + " - "
      + actor.context.sender().path)

    val event = ObjetoVinculoEvent.CreatedObjetoVinculoFromObj(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30Objeto
    )

    implicit val ssytem: ActorSystem = actor.context.system
    //todo para mandar mensajes a todos los objetos de los distintos vinculos
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)


    actor.persistEvent(event) { () =>

      actor.state += event
      println("map -> " + actor.state.map)
      actor.state.map.foreach {
        e => {
          println("vin -> " + e._1)
          actorSujetoGeneral ! CommandTest(0, e._1.sujetoId, e._1.objetoId, e._1.tipoObj, )
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

// objeto -> transf -> objeto

// tranf -> objeto -> tranf