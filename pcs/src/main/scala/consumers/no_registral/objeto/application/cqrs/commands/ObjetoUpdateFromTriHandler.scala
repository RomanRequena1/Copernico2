package consumers.no_registral.objeto.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo.DmnObjeto
import design_principles.actor_model.mechanism.DeliveryIdManagement._
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromTri
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
            case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
            case None => None
          }
    val semaforo_color = command.registro.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR

    def isTipo(cmd: ObjetoUpdateFromTri) = {

      val result = DMNTreintaPorcientoTipo.calcularDmn(DmnObjeto(cmd.registro.SOJ_TIPO_OBJETO,
        cmd.registro.SOJ_ADHERIDO_DEBITO.getOrElse(""),
        cmd.registro.SOJ_ESTADO.getOrElse(""),
        cmd.registro.SOJ_TITULARIDAD.getOrElse(""),
        semaforo_color.getOrElse(""),
        "",
        ""
      ))

      result match {
        case f if f.equals(2) =>
          ("2", 2)
        case f if f.equals(-1) =>
          ("1", -1)

        case f if f.equals(1) =>
          ("1", 1)

      }
//      DMNTreintaPorcientoTipo.dmn(cmd) match {
//        case f if f.equals(2) =>
//          ("2", 2)
//        case f if f.equals(-1) =>
//          ("1", -1)
//
//        case f if f.equals(1) =>
//          ("1", 1)
//
//      }
    }
    val dmn = isTipo(command)


    val event = ObjetoEvents.ObjetoUpdatedFromTri(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.registro,
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito,
      Some(dmn._1),
      Some(dmn._2)
    )
    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    } else {
      // because ObjetoNovedadCotitularidad, the event processor, needs this event to publish AddCotitular
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParent(command, actor.state)
        if (actor.state.eventCounter == eventCounterMax) {
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
//        actor.persistSnapshot(event, actor.state) { () =>
//          /*if (!actor.state.isResponsable) {
//            actor.removeObligaciones()
//          }*/
//          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
//
//        }
      }

    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
