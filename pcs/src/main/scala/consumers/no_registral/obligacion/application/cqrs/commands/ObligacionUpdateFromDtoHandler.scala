package consumers.no_registral.obligacion.application.cqrs.commands

import akka.persistence.SnapshotSelectionCriteria
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionUpdateFromDto
import consumers.no_registral.obligacion.application.entities.{
  DetallesObligacion,
  DetallesSupresiones,
  ListDetallesObligaciones,
  ListDetallesSupresiones,
  ObligacionExternalDto
}
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
          evento.BOB_OTROS_ATRIBUTOS match {
            case None => obligacionNuevoTest
            case Some(value) if value.BOB_DETALLES.nonEmpty =>
              val otros_atributos_updated = value.BOB_DETALLES.map(actualizarBB_BOBDetalles)
              campoEvento.set(obligacionNuevoTest, Some(ListDetallesObligaciones(otros_atributos_updated)))
          }
        } else if (campoEvento.getName == "BOB_SUPRESIONES") {
          evento.BOB_SUPRESIONES match {
            case None => obligacionNuevoTest
            case Some(value) if value.BOB_DETALLES_SUPRESIONES.nonEmpty =>
              val supresiones_updated = value.BOB_DETALLES_SUPRESIONES.map(actualizarBB_BOBDetallesSupresiones)
              campoEvento.set(obligacionNuevoTest, Some(ListDetallesSupresiones(supresiones_updated)))
          }
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
    def actualizarBB_BOBDetallesSupresiones(supresion: DetallesSupresiones): DetallesSupresiones = {
      val declaredFields = supresion.getClass.getDeclaredFields
      var supresionNueva = supresion

      declaredFields.foreach { campo =>
        val campoSupresion = supresionNueva.getClass.getDeclaredField(campo.getName)
        campoSupresion.setAccessible(true)

        if (campoSupresion.get(supresion).equals(Some("null"))) {
          campoSupresion.set(supresionNueva, None)
        } else if (campoSupresion.get(supresion).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoSupresion.set(supresionNueva, None)
        } else if (campoSupresion.get(supresion).equals(Some(999))) {
          campoSupresion.set(supresionNueva, None)
        }
      }
      supresionNueva
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
            case (Some(eventoValue), Some(estadoValue))
                if eventoValue.BOB_DETALLES.nonEmpty && estadoValue.BOB_DETALLES.nonEmpty =>
              val otros_atributos_updated = eventoValue.BOB_DETALLES.zip(estadoValue.BOB_DETALLES).map {
                case (eventoDetalle, estadoDetalle) => actualizarCC_BOBDetalles(eventoDetalle, estadoDetalle)
              }
              campoEvento.set(obligacionNuevoTest, Some(ListDetallesObligaciones(otros_atributos_updated)))
            case _ => obligacionNuevoTest
          }
        } else if (campoEvento.getName == "BOB_SUPRESIONES") {
          (evento.BOB_SUPRESIONES, estado.BOB_SUPRESIONES) match {
            case (Some(eventoValue), Some(estadoValue))
                if eventoValue.BOB_DETALLES_SUPRESIONES.nonEmpty && estadoValue.BOB_DETALLES_SUPRESIONES.nonEmpty =>
              val supresiones_updated =
                eventoValue.BOB_DETALLES_SUPRESIONES.zip(estadoValue.BOB_DETALLES_SUPRESIONES).map {
                  case (eventoSupresion, estadoSupresion) =>
                    actualizarCC_BOBDetallesSupresiones(eventoSupresion, estadoSupresion)
                }
              campoEvento.set(obligacionNuevoTest, Some(ListDetallesSupresiones(supresiones_updated)))
            case _ => obligacionNuevoTest
          }
        }
      }
      obligacionNuevoTest
    }
    def actualizarCC_BOBDetalles(atributosEvento: DetallesObligacion, atributosEstado: DetallesObligacion) = {
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

    def actualizarCC_BOBDetallesSupresiones(supresionEvento: DetallesSupresiones,
                                            supresionEstado: DetallesSupresiones): DetallesSupresiones = {
      val declaredFields = supresionEvento.getClass.getDeclaredFields
      var supresionNueva = supresionEvento

      declaredFields.foreach { campo =>
        val campoEvento = supresionNueva.getClass.getDeclaredField(campo.getName)
        val campoEstado = supresionEstado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)

        if (campoEvento.get(supresionEvento) == None) {
          campoEvento.set(supresionNueva, campoEstado.get(supresionEstado))
        } else if (campoEvento.get(supresionEvento).equals(Some("null"))) {
          campoEvento.set(supresionNueva, None)
        } else if (campoEvento.get(supresionEvento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(supresionNueva, None)
        } else if (campoEvento.get(supresionEvento).equals(Some(999))) {
          campoEvento.set(supresionNueva, None)
        }
      }
      supresionNueva
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
      command.detallesSupresiones,
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
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -obligacion- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )

      // Informs that operation has been ignored */
      //todo check if this is desirable, why? signal the sender??

      // In this case the sender is "EL OBJETO"
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        if (!(initialization == "true" && command.registro.BOB_ESTADO.contains("ADMINISTRATIVA"))) {}
        if (event.registro.BOB_OTROS_ATRIBUTOS.get.BOB_DETALLES.head.tiene30Obligaciones.get.equals(true)) {
          actor.informParent(command)
        } else {
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
