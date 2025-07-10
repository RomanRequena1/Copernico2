package consumers.no_registral.objeto.application.cqrs.commands

import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.domain.ObjetoEvents.{DmnResumen, ObjetoUpdatedFromSujeto}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromSujetoHandler(actor: ObjetoActor)
  extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromSujeto] {

  def debeEnviarResumen(anterior: Option[Boolean], nuevo: Option[Boolean]): Boolean =
    anterior != nuevo

  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromSujeto
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )


    val obj_default = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"), None, None)

    val estado = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("")

    if (estado == "TRANSF") {
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      return Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }

    val aplicarDescuentoAnterior = actor.state.aplicarDescuento

    val event = ObjetoUpdatedFromSujeto(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto
    )

    actor.state += event

    def esTipoObjetoPermitido(tipo: String): Boolean =
      Set("A", "I", "N").contains(tipo)

    val eventDmn = DmnResumen(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      actor.state.registro.getOrElse(obj_default).SOJ_ID_EXTERNO.orElse(Some("None")),
      Some(actor.state.fechaUltMod),
      actor.state.aplicarDescuento,
      actor.state.dmnNumero,
      actor.state.dmnDescripcion
    )

    val result = DMNTreintaPorcientoFinal.calcularDmnFinal(
      DmnFinal(
        command.exclusionSUjeto,
        actor.state.exclusionObjeto,
        actor.state.clasificacionObjeto,
        actor.state.tiene30Objeto,
        actor.state.tiene30Sujeto.get,
        actor.state.tiene30ObjetoVinculo
      )
    )

    result match {
      case d if d.equals(true) =>
        val aplicarDescuentoNuevo = Some(true)
        val newState = actor.state.copy(aplicarDescuento = aplicarDescuentoNuevo)
        if (!actor.state.registro
          .getOrElse(obj_default)
          .SOJ_ESTADO
          .getOrElse("")
          .equals("BAJA") && newState.aplicarDescuento.isDefined) {
          actor.persistSnapshot(event, newState) { () =>
            if (debeEnviarResumen(aplicarDescuentoAnterior, aplicarDescuentoNuevo) && esTipoObjetoPermitido(command.tipoObjeto)) {
              actor.dmnresumenpersistSnapshot(eventDmn, newState) { () =>
                sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
              }
            } else {
              sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
            }
          }
        }
      case _ =>
        val aplicarDescuentoNuevo = Some(false)
        val newState = actor.state.copy(aplicarDescuento = aplicarDescuentoNuevo)
        if (!actor.state.registro
          .getOrElse(obj_default)
          .SOJ_ESTADO
          .getOrElse("")
          .equals("BAJA") && newState.aplicarDescuento.isDefined) {
          actor.persistSnapshot(event, newState) { () =>
            if (debeEnviarResumen(aplicarDescuentoAnterior, aplicarDescuentoNuevo) && esTipoObjetoPermitido(command.tipoObjeto)) {
              actor.dmnresumenpersistSnapshot(eventDmn, newState) { () =>
                sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
              }
            } else {
              sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
            }
          }
        }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}