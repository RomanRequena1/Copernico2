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
      command.deliveryId,
      command.dmnNumero,
      command.dmnDescripcion
    )

    implicit val system: ActorSystem = actor.context.system
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)

    actor.persistEvent(event) { () =>
      actor.state += event

      val todosLosVinculos = actor.state.mapVinculo ++ actor.state.mapTransf

      // FIX: Calcular tiene30ObjetoVinculo global considerando TODOS los vínculos
      // true = sin deuda, false = con deuda
      // Solo es true si TODOS los vínculos están sin deuda
      val tiene30ObjetoVinculoGlobal = todosLosVinculos.values.forall(_.tiene30Objeto)

      log.info(s"[VINCULO-CREATE-CALC] objetoId=${command.objetoId} - " +
        s"totalVinculos=${todosLosVinculos.size}, " +
        s"tiene30ObjetoVinculoGlobal=$tiene30ObjetoVinculoGlobal, " +
        s"vinculos=${todosLosVinculos.map { case (k, v) => s"${k.sujetoId}:${v.tiene30Objeto}" }.mkString(", ")}")

      todosLosVinculos.foreach { case (key, vinculoInfo) =>
        // FIX: Usar el valor global calculado
        val tiene30Final = tiene30ObjetoVinculoGlobal

        log.info(s"[VINCULO-CREATE-SEND] Enviando a sujetoId=${key.sujetoId}, objetoId=${key.objetoId}, " +
          s"tiene30Final=$tiene30Final (vinculoInfo.tiene30Objeto=${vinculoInfo.tiene30Objeto})")

        actorSujetoGeneral.ask[Response.SuccessProcessing](
          UpdateState30ObjetoFromObjVinculo(
            command.deliveryId,
            key.sujetoId,
            key.objetoId,
            key.tipoObj,
            tiene30Final,
            command.exclusionObjeto,
            command.idExterno,
            command.dmnNumero,
            command.dmnDescripcion
          )
        )
      }

      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}