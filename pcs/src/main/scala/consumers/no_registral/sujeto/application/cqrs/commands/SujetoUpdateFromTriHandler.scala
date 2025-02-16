package consumers.no_registral.sujeto.application.cqrs.commands

import consumers.no_registral.sujeto.application.entity.SujetoCommands.SujetoUpdateFromTri
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.application.helper.{SendToObjeto, SendToObjetoFromSujeto}
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoUpdatedFromTri
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import java.time.LocalDateTime
import scala.util.{Success, Try}

class SujetoUpdateFromTriHandler(actor: SujetoActor) extends SyncCommandHandler[SujetoUpdateFromTri] {
  override def handle(command: SujetoUpdateFromTri): Try[Response.SuccessProcessing] = {

    val sender = actor.context.sender()
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    def getBBParams(evento: SujetoExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      val sujetoNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = sujetoNuevoTest.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)

        if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(sujetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(sujetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(999))) {
          campoEvento.set(sujetoNuevoTest, None)
        }
      }
      sujetoNuevoTest
    }

    def getCCParams(evento: SujetoExternalDto, estado: SujetoExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      val sujetoNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = sujetoNuevoTest.getClass.getDeclaredField(campo.getName)
        val campoEstado = estado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)
        if (campoEvento.get(evento) == None) {
          campoEvento.set(sujetoNuevoTest, campoEstado.get(estado))
        } else if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(sujetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(sujetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(999))) {
          campoEvento.set(sujetoNuevoTest, None)
        }
      }
      sujetoNuevoTest
    }

    def registroNuevo() = {
      val registroFFF = actor.state.registro match {
        case None => getBBParams(command.registro)
        case Some(value) => getCCParams(command.registro, value)
      }
      registroFFF
    }

    val event = SujetoUpdatedFromTri(command.deliveryId, command.sujetoId, registroNuevo())

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.name} | ${actor.persistenceId}] respond idempotent because of old delivery id | $command")
      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)
    } else {

//      Sujeto: estado.exclusionSujeto = E , evento.exclusionSujeto = "". Cambio, informa al objeto
//      Sujeto: estado.exclusionSujeto = "", evento.exclusionSujeto = "E". Cambio, informa al objeto
      if (command.registro.SUJ_TIPO_EXCLUSION != actor.state.exclusionSujeto) {
        SendToObjetoFromSujeto(actor.state,
                               sender,
                               actor.context,
                               command.sujetoId,
                               command.registro.SUJ_TIPO_EXCLUSION)
      }

      actor.persistEvent(event) { () =>
        actor.state += event
        if (actor.state.eventCounter == eventCounterMax) {
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
        actor.persistSnapshot() { _ =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
