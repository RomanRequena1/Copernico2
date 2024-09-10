package consumers.no_registral.obligacion.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.application.entities.{DetallesObligacion, ListDetallesObligaciones, ObligacionExternalDto}
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionUpdatedFromDto
import consumers.no_registral.obligacion.infrastructure.dependency_injection.ObligacionActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement.isIdempotent

import java.time.LocalDateTime
import scala.util.{Success, Try}

class ObligacionUpdateFromDtoHandler(actor: ObligacionActor) extends SyncCommandHandler[ObligacionUpdateFromDto] {
  override def handle(command: ObligacionUpdateFromDto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

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
          val otros_atributos_evento =
            evento.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head

          val otros_atributos_updated =
            actualizarBB_BOBDetalles(otros_atributos_evento)

          campoEvento.set(obligacionNuevoTest, Some(ListDetallesObligaciones(List(otros_atributos_updated))))
        }
      }
      obligacionNuevoTest
    }
    def actualizarBB_BOBDetalles(atributosEvento: DetallesObligacion) = {

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
          estado.BOB_OTROS_ATRIBUTOS match {
            // FIXME: si el none _ continua la funcion
            case None => ()
            case Some(value) if value.BOB_DETALLES.nonEmpty => {
              val otros_atributos_evento =
                evento.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head

              val otros_atributos_estado =
                value.BOB_DETALLES.head

              //Option[ListDetallesObjeto]
              val otros_atributos_updated =
                actualizarCCBOBDetalles(otros_atributos_evento, otros_atributos_estado)

              campoEvento.set(obligacionNuevoTest, Some(ListDetallesObligaciones(List(otros_atributos_updated))))
            }
          }
        }
      }
      obligacionNuevoTest
    }
    def actualizarCCBOBDetalles(atributosEvento: DetallesObligacion, atributosEstado: DetallesObligacion) = {

      val declaredFields = atributosEvento.getClass.getDeclaredFields
      var atributosNuevo = atributosEvento

      declaredFields.foreach { campo =>
        val campoEvento = atributosNuevo.getClass.getDeclaredField(campo.getName)
        val campoEstado = atributosEstado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)

        if (campoEvento.get(atributosEvento) == None) {
          campoEvento.set(atributosNuevo, campoEstado.get(atributosEstado))
          // FIXME: validar q no rompe si el campo no estaba en el state
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

    //TODO Validate the first event, with no state, enters in the case None.
    def getObligacionFFF() = {
      val obligacionFFF = actor.state.registro match {
        case None => getBBParams(command.registro)
        case Some(value) => {
          getCCParams(command.registro, value)
        }
      }
      obligacionFFF
    }

    val event = ObligacionUpdatedFromDto(
      command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.obligacionId,
      getObligacionFFF(),
      command.detallesObligacion,
      command.isAdheridoDebito,
      command.cuota,
      command.resultDmn
    )
    // check whether we are in initialization mode or not
    val initialization: String = {
      Try(System.getenv("INITIALIZATION")).getOrElse(null)
    }

    //val eventCounterMax = Try(System.getenv("EVENT-COUNTER-MAX")).getOrElse(9)

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(s"[${actor.name} | ${actor.persistenceId}] -obligacion- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)

      // Informs that operation has been ignored */
      //todo check if this is desirable, why? signal the sender??

      // In this case the sender is "EL OBJETO"
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        if (!(initialization == "true" && command.registro.BOB_ESTADO.contains("ADMINISTRATIVA"))) {
        }
        if (event.registro.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head.tiene30Obligaciones.get.equals(true)) {
          actor.informParent(command)
        }

        else {
          actor.informParentTreintaProciento(event)
        }

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
