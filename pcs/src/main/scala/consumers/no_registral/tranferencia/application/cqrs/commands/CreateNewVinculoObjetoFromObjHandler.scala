package consumers.no_registral.tranferencia.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.UpdateState30ObjetoFromObjVinculo
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.CreateTransfVinculoObjetoFromObj
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class CreateNewVinculoObjetoFromObjHandler(actor: ObjetoVinculoActor, tranferenciaActorRequirements: MonitoringAndMessageProducer) extends SyncCommandHandler[CreateTransfVinculoObjetoFromObj] {
  override def handle(command: CreateTransfVinculoObjetoFromObj): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    println("Llego? CreateNewVinculoObjetoFromObjHandler::::: " + command + " - "
      + command.tiene30Objeto + " - "
      + actor.context.self.path + " - "
      + actor.context.sender().path)

    val event = ObjetoVinculoEvent.CreatedTransfVinculoObjetoFromObj(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30Objeto,
      command.isResponsable,
      command.estadoObj,
      command.titularidad
    )

    implicit val ssytem: ActorSystem = actor.context.system
    //todo para mandar mensajes a todos los objetos de los distintos vinculos
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)


    actor.persistEvent(event) { () =>
      //todo a quien mando los mensajes? jajaja
      actor.state += event
      println("mapTransf -> " + actor.state.mapTransf)
      println("mapVinculo mapTransf-> " + actor.state.mapVinculo)
      actor.state.mapVinculo.foreach {
        e => {
          println("vin -> " + e._1)
          if(!e._2.estado.getOrElse("").equals("TRANSF"))
            actorSujetoGeneral ! UpdateState30ObjetoFromObjVinculo(0, e._1.sujetoId, e._1.objetoId, e._1.tipoObj, actor.state.tiene30ObjetoVinculo)
        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

// objeto -> transf -> objeto

// tranf -> objeto -> tranf