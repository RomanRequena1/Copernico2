package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.ActorRef
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.domain.ObjetoEvents.{AplicarDescuentoUpdated, DmnResumen, ObjetoUpdatedFromSujeto}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromSujetoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromSujeto] {

  private val resumenEnabled: Option[String] = Option(System.getenv("KAFKA_BROKERS_LIST_PSRM"))

  override def handle(command: ObjetoCommands.ObjetoUpdateFromSujeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val obj_default: ObjetosTri = ObjetosTri(
      Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"),
      None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"),
      Some("None"), Some("None"), Some("None"), Some("None"), Some("None"),
      None, None
    )

    val estado = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("")
    if (estado == "TRANSF" || estado == "ESTADO2") {
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      return Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }

    val event = ObjetoUpdatedFromSujeto(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto,
      command.dmnDescripcionSujeto,
      command.idExterno,
      command.deliveryIdObligacion
    )

    actor.state += event

    def esTipoObjetoPermitido(tipo: String): Boolean =
      Set("A", "I", "N").contains(tipo)

    val result: Boolean = DMNTreintaPorcientoFinal.calcularDmnFinal(
      DmnFinal(
        command.exclusionSUjeto,
        actor.state.exclusionObjeto,
        actor.state.clasificacionObjeto,
        actor.state.tiene30Objeto,
        actor.state.tiene30Sujeto.get,
        actor.state.tiene30ObjetoVinculo
      )
    )

    val aplicarDescuentoNuevo = Some(result)
    val aplicarDescuentoAnterior = actor.state.aplicarDescuento

    val event1 = AplicarDescuentoUpdated(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      aplicarDescuentoNuevo
    )

    actor.state += event1

    val cambioDeMarcaEnObjeto = aplicarDescuentoAnterior != aplicarDescuentoNuevo

    val isLossByOtherCambio = cambioDeMarcaEnObjeto &&
      aplicarDescuentoAnterior.contains(true) &&
      aplicarDescuentoNuevo.contains(false) &&
      actor.state.tiene30Objeto

    val isNoDebtButLosesBySujeto = aplicarDescuentoNuevo.contains(false) &&
      actor.state.tiene30Objeto &&
      actor.state.tiene30ObjetoVinculo &&
      !actor.state.tiene30Sujeto.getOrElse(true)

    val isNoDebtButLosesByVinculo = aplicarDescuentoNuevo.contains(false) &&
      actor.state.obligaciones.isEmpty &&
      !actor.state.tiene30ObjetoVinculo

    val isLossByOther = isLossByOtherCambio || isNoDebtButLosesBySujeto || isNoDebtButLosesByVinculo

    val (dmnNumeroParaPSRM, dmnDescripcionParaPSRM) = {
      if (isLossByOther) {
        val motivo = if (isNoDebtButLosesByVinculo) {
          "No cumple por deuda en otro objeto del vínculo"
        } else {
          "No cumple por deuda en otro objeto del sujeto"
        }
        (Some(99), Some(motivo))
      } else if (aplicarDescuentoNuevo.contains(false)) {
        if (actor.state.dmnNumero.isDefined && !actor.state.dmnNumero.contains(1) && !actor.state.dmnNumero.contains(0)) {
          (actor.state.dmnNumero, actor.state.dmnDescripcion)
        } else {
          command.dmnDescripcionSujeto.filter(d => d.nonEmpty && d != "Deuda") match {
            case Some(desc) => (Some(-1), Some(desc))
            case None => (Some(99), Some("No cumple por deuda en otro objeto"))
          }
        }
      } else {
        if (actor.state.dmnNumero.isDefined && !actor.state.dmnNumero.contains(0)) {
          (actor.state.dmnNumero, actor.state.dmnDescripcion)
        } else {
          (Some(1), Some("No Deuda"))
        }
      }
    }

    val eventDmn = DmnResumen(
      command.deliveryIdObligacion.getOrElse(0),
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      actor.state.idExterno,
      Some(actor.state.fechaUltMod),
      actor.state.aplicarDescuento,
      dmnNumeroParaPSRM,
      dmnDescripcionParaPSRM
    )

    if (!actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("").equals("BAJA") &&
      actor.state.aplicarDescuento.isDefined &&
      esTipoObjetoPermitido(command.tipoObjeto) &&
      resumenEnabled.isDefined &&
      cambioDeMarcaEnObjeto &&
      command.objetoId.nonEmpty)
    {
      actor.persistSnapshot(event, actor.state) { () =>
        implicit val system = actor.context.system
        val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

        vinculoActor ! ObjetoVinculoCommands.AuditarYEnviarResumen(
          deliveryId = command.deliveryId,
          objetoId = command.objetoId,
          tipoObj = command.tipoObjeto,
          sujetoId = command.sujetoId,
          aplicarDescuento = actor.state.aplicarDescuento,
          idExterno = command.idExterno,
          eventDmn = eventDmn
        )

        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    } else {
      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}