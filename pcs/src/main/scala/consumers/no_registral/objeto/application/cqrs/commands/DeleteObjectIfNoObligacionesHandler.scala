package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosAnt
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class DeleteObjectIfNoObligacionesHandler(actor: ObjetoActor, requirements: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.DeleteObjectIfNoObligaciones] {

  override def handle(command: ObjetoCommands.DeleteObjectIfNoObligaciones): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    if (actor.state.isBaja) {
      log.warn(s"El objeto ${command.objetoId} ya fue eliminado previamente")
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      return Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }

    val eventRemove = ObjetoEvents.ObjetoRemovedObligacion(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      command.cuota,
      actor.state.dmnNumero,
      actor.state.dmnDescripcion
    )

    actor.persistEvent(eventRemove) { () =>
      actor.state += eventRemove


      val obligacionesRestantes = actor.state.obligacionesSaldo


      if (obligacionesRestantes.isEmpty && (
        command.tipoObjeto == "PPP" ||
          command.tipoObjeto == "PM26" ||
          command.tipoObjeto == "PVS" ||
          command.tipoObjeto == "MVD" ||
          command.tipoObjeto == "LTE" ||
          command.tipoObjeto == "BDG"
        )) {
        log.warn(s"Eliminando objeto ANT ${command.tipoObjeto} ${command.objetoId} - No quedan obligaciones")
        val obj_default: ObjetosAnt = ObjetosAnt(
          RULE_NUMBER = None,
          EV_ID = command.deliveryId,
          SOJ_SUJ_IDENTIFICADOR = command.sujetoId,
          SOJ_TIPO_OBJETO = command.tipoObjeto,
          SOJ_IDENTIFICADOR = command.objetoId,
          SOJ_CAT_SOJ_ID = None,
          SOJ_DESCRIPCION = Some("DefaultANT"),
          SOJ_ESTADO = None,
          SOJ_FECHA_INICIO = None,
          SOJ_FECHA_FIN = None,
          SOJ_ID_EXTERNO = None,
          SOJ_OTROS_ATRIBUTOS = None,
          SOJ_BASE_IMPONIBLE = None,
          SOJ_ADHERIDO_DEBITO = None,
          SOJ_CANT_CUOTAS_PAGADAS = None,
          SOJ_CANAL_ORIGEN = None,
          SOJ_SUBTIPO = None,
          SOJ_IDENTIFICADOR_2 = None,
          SOJ_DOCUMENTO = None,
          SOJ_TITULARIDAD = None,
          SOJ_TIPO_EXCLUSION = None,
          SOJ_FECHA_VTA_SUBASTA = None,
          SOJ_FECHA_ADQ_SUBASTA = None)

        actor.self ! ObjetoCommands.SetBajaObjeto(
          sujetoId = command.sujetoId,
          objetoId = command.objetoId,
          tipoObjeto = command.tipoObjeto,
          deliveryId = command.deliveryId,
          registro = actor.state.registro match {
            case Some(reg) =>
              reg match {
                case ant: ObjetosAnt => ant.copy(SOJ_ESTADO = Some("BAJA"))
                case other => obj_default
              }
            case None => obj_default
          },
          isResponsable = None,
          sujetoResponsable = None
        )
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      } else {
        // Si no se elimina, actualizamos el snapshot con la obligación removida
        actor.persistSnapshot(eventRemove, actor.state) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}