package consumers.no_registral.obligacion.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionAntUpdateFromDto
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ListDetallesObligaciones, ObligacionExternalDto}
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionAntUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import java.time.LocalDateTime
import scala.util.{Success, Try}

class ObligacionAntUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionAntUpdateFromDto] {
  override def handle(command: ObligacionAntUpdateFromDto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    def getBBParams(evento: ObligacionExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      var obligacionNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = obligacionNuevoTest.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(obligacionNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(obligacionNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(999))) {
          campoEvento.set(obligacionNuevoTest, None)
        } else if (campoEvento.getName == "BOB_OTROS_ATRIBUTOS") {
          evento.BOB_OTROS_ATRIBUTOS match {
            case None => obligacionNuevoTest
            case Some(value) if value.BOB_DETALLES.nonEmpty =>
              val otros_atributos_updated = value.BOB_DETALLES.map(actualizarBBBobDetalles)
              campoEvento.set(obligacionNuevoTest, Some(ListDetallesObligaciones(otros_atributos_updated)))
          }
        }
      }
      obligacionNuevoTest
    }
    def actualizarBBBobDetalles(atributosEvento: DetallesObligacion) = {

      val declaredFields = atributosEvento.getClass.getDeclaredFields
      var atributosNuevo = atributosEvento

      declaredFields.foreach { campo =>
        val campoEvento = atributosNuevo.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)

        if (campoEvento.get(atributosEvento).equals(Some("null"))) {
          campoEvento.set(atributosNuevo, None)
        } else if (campoEvento.get(atributosEvento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(atributosNuevo, None)
        } else if (campoEvento.get(atributosEvento).equals(Some(999))) {
          campoEvento.set(atributosNuevo, None)
        }
      }
      atributosNuevo
    }

    def getCCParams(evento: ObligacionExternalDto, estado: ObligacionExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      var obligacionNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = obligacionNuevoTest.getClass.getDeclaredField(campo.getName)
        val campoEstado = estado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)
        if (campoEvento.get(evento) == None) {
          campoEvento.set(obligacionNuevoTest, campoEstado.get(estado))
        } else if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(obligacionNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(obligacionNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(999))) {
          campoEvento.set(obligacionNuevoTest, None)
        } else if (campoEvento.getName == "BOB_OTROS_ATRIBUTOS") {
          (evento.BOB_OTROS_ATRIBUTOS, estado.BOB_OTROS_ATRIBUTOS) match {
            case (Some(eventoValue), Some(estadoValue)) if eventoValue.BOB_DETALLES.nonEmpty && estadoValue.BOB_DETALLES.nonEmpty =>
              val otros_atributos_updated = eventoValue.BOB_DETALLES.zip(estadoValue.BOB_DETALLES).map {
                case (eventoDetalle, estadoDetalle) => actualizarCCBobDetalles(eventoDetalle, estadoDetalle)
              }
              campoEvento.set(obligacionNuevoTest, Some(ListDetallesObligaciones(otros_atributos_updated)))
            case _ => obligacionNuevoTest
          }
        }
      }
      obligacionNuevoTest
    }

    def actualizarCCBobDetalles(atributosEvento: DetallesObligacion, atributosEstado: DetallesObligacion) = {

      val declaredFields = atributosEvento.getClass.getDeclaredFields
      var atributosNuevo = atributosEvento

      declaredFields.foreach { campo =>
        val campoEvento = atributosNuevo.getClass.getDeclaredField(campo.getName)
        val campoEstado = atributosEstado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)

        if (campoEvento.get(atributosEvento) == None) {
          campoEvento.set(atributosNuevo, campoEstado.get(atributosEstado))
        } else if (campoEvento.get(atributosEvento).equals(Some("null"))) {
          campoEvento.set(atributosNuevo, None)
        } else if (campoEvento.get(atributosEvento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(atributosNuevo, None)
        } else if (campoEvento.get(atributosEvento).equals(Some(999))) {
          campoEvento.set(atributosNuevo, None)
        }
      }
      atributosNuevo
    }

    def getObligacionAntFFF() = {
      val obligacionFFF = actor.state.registro match {
        case None => getBBParams(command.registro)
        case Some(value) => {
          getCCParams(command.registro, value)
        }
      }
      obligacionFFF
    }

    val event = ObligacionAntUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      getObligacionAntFFF(),
      command.detallesObligacion,
      command.detallesSupresiones,
      command.isAdheridoDebito,
      command.cuota
    )
    // check whether we are in initialization mode or not
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -obligacionAnt- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        if (!(initialization == "true" && command.registro.BOB_ESTADO.contains("ADMINISTRATIVA"))) {
          //actor.informParent(command)
        }
        actor.informParent(command)

        if (actor.state.eventCounter == eventCounterMax) {
          actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 2))
          actor.saveSnapshot(actor.state.copy(eventCounter = 0))
        }
        actor.lastDeliveryId = command.registro.EV_ID
        actor.persistSnapshot(event) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

        }
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
