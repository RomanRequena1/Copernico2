package consumers.no_registral.tranferencia.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.{MonitoringAndMessageProducer, MonitoringAndMessageProducerTranf}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.CommandTest
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.application.entity.TransferenciaCommands._
import consumers.no_registral.tranferencia.domain.TransferenciaEvent
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.TranferenciaActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class CreateVinculoObjSujToTransfHandler(actor: TranferenciaActor, tranferenciaActorRequirements: MonitoringAndMessageProducer) extends SyncCommandHandler[CreateVinculoObjSujToTransf] {
  override def handle(command: CreateVinculoObjSujToTransf): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    println("Llego? CreateVinculoObjSujToTransfHandler::::: " + command + " - " + command.tiene30ObjetoTranf)
    val event = TransferenciaEvent.CreatedVinculoObjSujToTransf(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30ObjetoTranf
    )

    implicit val act: ActorSystem = actor.context.system
    //todo para mandar mensajes a todos los objetos de los distintos vinculos
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)


    val actorSelection = actor.context.actorSelection(s"akka://PersonClassificationService/system/sharding/SujetoActor/*/${command.sujetoId}/Sujeto-${command.sujetoId}-Objeto-${command.objetoId}-${command.tipoObj}")

    actorSelection ! CommandTest(0, command.sujetoId, command.objetoId, command.tipoObj)


    actor.persistEvent(event) { () =>

      actor.state += event
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

// objeto -> transf -> objeto

// tranf -> objeto -> tranf