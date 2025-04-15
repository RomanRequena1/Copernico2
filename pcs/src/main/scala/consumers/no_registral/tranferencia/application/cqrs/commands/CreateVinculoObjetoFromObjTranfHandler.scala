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
//import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotentInternally

import scala.util.{Success, Try}

class CreateVinculoObjetoFromObjTranfHandler(actor: ObjetoVinculoActor,
                                             tranferenciaActorRequirements: MonitoringAndMessageProducer)
    extends SyncCommandHandler[CreateTransfVinculoObjetoFromObj] {
  override def handle(command: CreateTransfVinculoObjetoFromObj): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val event = ObjetoVinculoEvent.CreatedTransfVinculoObjetoFromObj(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30Objeto,
      command.isResponsable,
      command.estadoObj,
      command.titularidad,
      command.exclusionObjeto,
      command.deliveryId
    )
//    if (isIdempotentInternally(command, actor.state.lastDeliveryIdByEvents)) {
//      log.warn(
//        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond internally_idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
//      )
//      sender ! Response.SuccessProcessing("IDEM-INT-" + command.aggregateRoot, command.deliveryId)
//
//    } else {
//      implicit val system: ActorSystem = actor.context.system
//      implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)
//
//      actor.persistEvent(event) { () =>
//        actor.state += event
//
//        //Recorre el map de vinculos y manda mensaje a los objetos
//        actor.state.mapVinculo.foreach { e =>
//          {
//
//            actorSujetoGeneral.ask[Response.SuccessProcessing](
//              UpdateState30ObjetoFromObjVinculo(0,
//                                                e._1.sujetoId,
//                                                e._1.objetoId,
//                                                e._1.tipoObj,
//                                                actor.state.tiene30ObjetoVinculo,
//                                                command.exclusionObjeto)
//            )
//          }
//        }
//        actor.persistSnapshot(event, actor.state) { () =>
//          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//        }
//      }
//    }

    implicit val system: ActorSystem = actor.context.system
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)

    actor.persistEvent(event) { () =>
      actor.state += event

      //Recorre el map de vinculos y manda mensaje a los objetos
      actor.state.mapVinculo.foreach { e =>
      {

        actorSujetoGeneral.ask[Response.SuccessProcessing](
          UpdateState30ObjetoFromObjVinculo(0,
            e._1.sujetoId,
            e._1.objetoId,
            e._1.tipoObj,
            actor.state.tiene30ObjetoVinculo,
            command.exclusionObjeto)
        )
      }
      }
      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}

