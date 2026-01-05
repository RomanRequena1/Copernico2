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

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class CreateVinculoObjetoFromObjTranfHandler(
                                              actor: ObjetoVinculoActor,
                                              tranferenciaActorRequirements: MonitoringAndMessageProducer
                                            ) extends SyncCommandHandler[CreateTransfVinculoObjetoFromObj] {

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
      command.idExterno,
      command.deliveryId
    )

    implicit val system: ActorSystem = actor.context.system
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)
    implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global


    actor.persistEvent(event) { () =>
      actor.state += event
      val tieneDeuadEnTransf = actor.state.mapTransf.exists(_._2.tiene30Objeto == false)

      actor.state.mapVinculo.foreach { e =>
      {
        // si hay deuda de una transferencia hay que pasar la marca a false para nuevos titulares
        val tiene30Final = if (tieneDeuadEnTransf) {
          false  // responsabilidad solidaria adquirir deuda
        } else {
          actor.state.tiene30ObjetoVinculo  // sino dejar la marca como estaba
        }

        actorSujetoGeneral.ask[Response.SuccessProcessing](
          UpdateState30ObjetoFromObjVinculo(
            command.deliveryId,
            e._1.sujetoId,
            e._1.objetoId,
            e._1.tipoObj,
            tiene30Final,  // valor calculado según deuda
            command.exclusionObjeto,
            command.idExterno,
            command.dmnNumero,
            command.dmnDescripcion
          )
        )}
      }
      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}