package consumers.no_registral.tranferencia.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.UpdateState30ObjetoFromObjVinculo
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.UpdateVinculoObjetoFromObj
import consumers.no_registral.tranferencia.domain.{ObjetoVinculoEvent, Vinculo}
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

    log.info(s"[VINCULO-RECEIVED] Comando UpdateVinculoObjetoFromObj recibido - objetoId=${command.objetoId}, sujetoId=${command.sujetoId}")

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
      command.deliveryId
    )

    implicit val ssytem: ActorSystem = actor.context.system
    implicit val actorSujetoGeneral: ActorRef = SujetoActor.startWithRequirements(tranferenciaActorRequirements)

    actor.persistEvent(event) { () =>
      actor.state += event

      val tieneDeudaEnTransf = actor.state.mapTransf.exists(_._2.tiene30Objeto == false)
      val todosLosVinculos = actor.state.mapVinculo ++ actor.state.mapTransf

      todosLosVinculos.foreach { e => {
        val tiene30Final: Boolean = {
          if (tieneDeudaEnTransf) {
            false
          } else {
            actor.state.tiene30ObjetoVinculo
          }
        }

        actorSujetoGeneral.ask[Response.SuccessProcessing](
          UpdateState30ObjetoFromObjVinculo(
            0,
            e._1.sujetoId,
            e._1.objetoId,
            e._1.tipoObj,
            tiene30Final,
            command.exclusionObjeto
          )
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