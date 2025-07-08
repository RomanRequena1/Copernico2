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

    val event = ObjetoUpdatedFromSujeto(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto
    )

    actor.state += event

    def debeEnviarResumen: Boolean = {
      if (actor.state.ultimo30Objeto.size > 1) {
        val penultimoElemento = actor.state.ultimo30Objeto.toSeq.reverse.tail.head._2
        !penultimoElemento.equals(actor.state.tiene30Objeto)
      } else false
    }

    def esTipoObjetoPermitido(tipo: String): Boolean =
      Set("A", "I", "N").contains(tipo)

    val obj_default = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"), None, None)


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
        val newState = actor.state.copy(aplicarDescuento = Some(true))
        if (!actor.state.registro
          .getOrElse(obj_default)
          .SOJ_ESTADO
          .getOrElse("")
          .equals("BAJA") && newState.aplicarDescuento.isDefined) {
          actor.persistSnapshot(event, newState) { () =>
            actor.dmnresumenpersistSnapshot(eventDmn, newState) { () =>
              sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
            }
          }
        }
      case _ =>
        val newState = actor.state.copy(aplicarDescuento = Some(false))
        if (!actor.state.registro
          .getOrElse(obj_default)
          .SOJ_ESTADO
          .getOrElse("")
          .equals("BAJA") && newState.aplicarDescuento.isDefined) {
          actor.persistSnapshot(event, newState) { () =>
            if (debeEnviarResumen && esTipoObjetoPermitido(command.tipoObjeto)) {
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
