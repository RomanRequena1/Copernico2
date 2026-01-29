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

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success, Try}

class UpdateObjetoVinculoFromObjHandler(
                                         actor: ObjetoVinculoActor,
                                         tranferenciaActorRequirements: MonitoringAndMessageProducer
                                       ) extends SyncCommandHandler[UpdateVinculoObjetoFromObj] {

  override def handle(command: UpdateVinculoObjetoFromObj): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    println(s"[VINCULO-RECEIVED] Comando UpdateVinculoObjetoFromObj recibido - objetoId=${command.objetoId}, sujetoId=${command.sujetoId}, deliveryId=${command.deliveryId}")

    log.debug(
      f"""|CUMBIA - UpdateVinculoObjetoFromObj
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    val event = ObjetoVinculoEvent.UpdatedVinculoObjetoFromObj(
      command.sujetoId,
      command.objetoId,
      command.tipoObj,
      command.tiene30Objeto,
      command.isResponsable,
      command.estadoObj,
      command.titularidad,
      command.exclusionObjeto,
      command.deliveryId,
      command.idExterno,
      command.dmnNumero,
      command.dmnDescripcion
    )

    implicit val system: ActorSystem = actor.context.system
    implicit val ec: ExecutionContext = actor.context.dispatcher
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)

    actor.persistEvent(event) { () =>
      actor.state += event

      val todosLosVinculos = actor.state.mapVinculo ++ actor.state.mapTransf

      val tiene30ObjetoVinculoGlobal = todosLosVinculos.values.forall(_.tiene30Objeto)

      println(s"[VINCULO-UPDATE-CALC] objetoId=${command.objetoId}, deliveryId=${command.deliveryId} - " +
        s"totalVinculos=${todosLosVinculos.size}, " +
        s"tiene30ObjetoVinculoGlobal=$tiene30ObjetoVinculoGlobal, " +
        s"vinculos=${todosLosVinculos.map { case (k, v) => s"${k.sujetoId}:${v.tiene30Objeto}" }.mkString(", ")}")

      todosLosVinculos.foreach { case (key, vinculoInfo) =>
        val tiene30Final = tiene30ObjetoVinculoGlobal

        println(s"[VINCULO-PRE-SEND] deliveryId=${command.deliveryId}, " +
          s"destino: sujetoId=${key.sujetoId}, objetoId=${key.objetoId}, tipoObj=${key.tipoObj}, " +
          s"tiene30Final=$tiene30Final")

        val futureResponse = actorSujetoGeneral.ask[Response.SuccessProcessing](
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

        futureResponse.onComplete {
          case Success(response) =>
            println(s"[VINCULO-SEND-OK] deliveryId=${command.deliveryId}, " +
              s"sujetoId=${key.sujetoId}, objetoId=${key.objetoId} - Response OK")
          case Failure(ex) =>
            println(s"[VINCULO-SEND-FAIL] deliveryId=${command.deliveryId}, " +
              s"sujetoId=${key.sujetoId}, objetoId=${key.objetoId} - ERROR: ${ex.getMessage}")
            ex.printStackTrace()
        }
      }

      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}