package consumers.no_registral.tranferencia.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.AuditarYEnviarResumen
import consumers.no_registral.tranferencia.domain.ObjetoVinculoEvent
import consumers.no_registral.tranferencia.infrastructure. dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import java.time.LocalDateTime
import scala.util.{Success, Try}

class AuditoriaHandler(actor:  ObjetoVinculoActor,
                       requirements: MonitoringAndMessageProducer
                      ) extends SyncCommandHandler[AuditarYEnviarResumen] {

  override def handle(command: AuditarYEnviarResumen): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val ultimoEnviadoAnterior = actor.state.ultimoAplicarDescuentoEnviado
    val cambioDeMarca = ultimoEnviadoAnterior != command.aplicarDescuento

    if (cambioDeMarca) {
      val event = ObjetoVinculoEvent.ResumenEnviado(
        command.eventDmn.deliveryId,
        command.objetoId,
        command.tipoObj,
        command.aplicarDescuento,
        LocalDateTime.now
      )

      actor.state += event

      actor.persistEvent(event) { () =>
        actor.dmnresumenpersistSnapshot(command.eventDmn, actor.state) { () =>
          log.info(s"[AUDITORIA-ENVIADO] objetoId=${command.objetoId}, aplicarDescuento=${command.aplicarDescuento}")
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    } else {
      log.info(s"[AUDITORIA-NO-ENVIADO] objetoId=${command. objetoId}, no hubo cambio de marca (ya estaba en $ultimoEnviadoAnterior)")
      sender ! Response. SuccessProcessing(command. aggregateRoot, command.deliveryId)
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}