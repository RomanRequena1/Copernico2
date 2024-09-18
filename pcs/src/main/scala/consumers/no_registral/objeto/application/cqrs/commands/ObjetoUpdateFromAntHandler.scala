package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetoExternalDto}
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._

import scala.util.{Success, Try}

class ObjetoUpdateFromAntHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromAnt] {
  override def handle(
      command: ObjetoCommands.ObjetoUpdateFromAnt
  ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    import java.time.LocalDateTime

    def getBBParams(evento: ObjetoExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      var objetoNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = objetoNuevoTest.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)

        if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(objetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(objetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(999))) {
          campoEvento.set(objetoNuevoTest, None)
        } else if (campoEvento.getName == "SOJ_OTROS_ATRIBUTOS") {
          evento.SOJ_OTROS_ATRIBUTOS match {
            case None => objetoNuevoTest
            case Some(value) if value.SOJ_DETALLES.nonEmpty => {
              val otros_atributos_evento =
                evento.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head

              val otros_atributos_updated =
                actualizarBBSojDetalles(otros_atributos_evento)

              campoEvento.set(objetoNuevoTest, Some(ListDetallesObjeto(List(otros_atributos_updated))))
            }
          }
        }
      }
      objetoNuevoTest
    }

    def actualizarBBSojDetalles(atributosEvento: DetallesObjeto) = {

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

    def getCCParams(evento: ObjetoExternalDto, estado: ObjetoExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      var objetoNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = objetoNuevoTest.getClass.getDeclaredField(campo.getName)
        val campoEstado = estado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)
        if (campoEvento.get(evento) == None) {
          campoEvento.set(objetoNuevoTest, campoEstado.get(estado))
        } else if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(objetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(LocalDateTime.of(1000, 1, 1, 0, 0, 0)))) {
          campoEvento.set(objetoNuevoTest, None)
        } else if (campoEvento.get(evento).equals(Some(999))) {
          campoEvento.set(objetoNuevoTest, None)
        } else if (campoEvento.getName == "SOJ_OTROS_ATRIBUTOS") {
          estado.SOJ_OTROS_ATRIBUTOS match {
            case None => objetoNuevoTest
            case Some(value) if value.SOJ_DETALLES.nonEmpty => {
              val otros_atributos_evento =
                evento.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head

              val otros_atributos_estado =
                value.SOJ_DETALLES.head

              //Option[ListDetallesObjeto]
              val otros_atributos_updated =
                actualizarCCSojDetalles(otros_atributos_evento, otros_atributos_estado)

              campoEvento.set(objetoNuevoTest, Some(ListDetallesObjeto(List(otros_atributos_updated))))
            }
          }
        }
      }
      objetoNuevoTest
    }

    def actualizarCCSojDetalles(atributosEvento: DetallesObjeto, atributosEstado: DetallesObjeto) = {

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

    def getObjetoFFF() = {
      val objetoFFF = actor.state.registro match {
        case None => getBBParams(command.registro)
        case Some(value) => {
          getCCParams(command.registro, value)
        }
      }
      objetoFFF
    }

    val event = ObjetoEvents.ObjetoUpdatedFromAnt(
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      getObjetoFFF(),
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito
    )
    // FIXME: Chequear si debemos sumar algun comportamiento del ObjetoUpdateFromTriHandler
    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

    } else {
      actor.persistEvent(event) { () =>
        actor.state += event
        actor.informParentAnt(actor.state.lastDeliveryIdByEvents,
                              command.sujetoId,
                              command.objetoId,
                              command.tipoObjeto,
                              actor.state)
        actor.persistSnapshot(event, actor.state) { () =>
          ()
        }
      }
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}
