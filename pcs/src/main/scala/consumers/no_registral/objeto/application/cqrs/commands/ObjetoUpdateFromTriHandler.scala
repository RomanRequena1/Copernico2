package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo.DmnObjeto
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.application.helper.SendObjetoToObjetoVinculo
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor,  requeriment: MonitoringAndMessageProducer) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {

  /**
   * Si el objeto es tipo M y actor.state.tiene30Objeto es false, entonces informParentTreintaPorciento y si actor.state.tiene30Objeto es true, entonces informParent.
   * En ambos casos, persistSnapshot.
   * En el caso del else, se envía el objeto a objeto vinculo.
   */
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
    def persistSnapshotEvent(): Success[Response.SuccessProcessing] = {

      actor.persistEvent(event) { () =>
        actor.state += event
        //todo juicio persiste, pero no se us apara el calculo del 30%?

        if (actor.state.registro.get.SOJ_TIPO_OBJETO.equals("M")) { // todo tipo M , pero si para el calculo de deuda para un sujeto. Objeto juicio queda atado a cuit, pero no se va a teber en cuanta cuando se calcule el 30%, no se guarda el vinculo.
          if (actor.state.tiene30Objeto.equals(false))
            actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state)
          else
            actor.informParent(actor.state.lastDeliveryIdByEvents, command.sujetoId, command.objetoId, command.tipoObjeto, actor.state)
          actor.persistSnapshot(event, actor.state) { () =>
            sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
          }
        }
        else {
          log.error("SEND OBJETO TO OBJETO VINCULO: " + SendObjetoToObjetoVinculo(actor, command.sujetoId, command.objetoId, command.tipoObjeto, command.registro.SOJ_ESTADO, requeriment))
          SendObjetoToObjetoVinculo(actor, command.sujetoId, command.objetoId, command.tipoObjeto, command.registro.SOJ_ESTADO, requeriment)
        }
        //actor.informParent(command, actor.state) //todo saque el infoparent, deberia hacer el nuevo handler
        if (actor.state.eventCounter == eventCounterMax) {
          actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 2))
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }

    command.deliveryId match {
      case x if (command.deliveryId.signum < 0 ) =>
        persistSnapshotEvent()

      case x if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) =>
        log.error("ENTRE AL EV_ID IDEMPOTENT DEL ALTA")

        log.error(s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

        Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
      case _ =>
        persistSnapshotEvent()
    }
  }
}
