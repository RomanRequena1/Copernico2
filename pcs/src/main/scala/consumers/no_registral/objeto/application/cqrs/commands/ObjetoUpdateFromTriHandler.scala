package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import akka.util.OptionVal
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.objeto.application.cqrs.commands.test.persistSnapshotEvent
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo.DmnObjeto
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetoExternalDto}
import consumers.no_registral.objeto.application.helper.SendObjetoToObjetoVinculo
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {

  /**
   * Si el objeto es tipo M y actor.state.tiene30Objeto es false, entonces informParentTreintaPorciento y si actor.state.tiene30Objeto es true, entonces informParent.
   * En ambos casos, persistSnapshot.
   * En el caso del else, se envía el objeto a objeto vinculo.
   */
  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromTri
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val log: Logger = LoggerFactory.getLogger(this.getClass)

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
      case None => None
    }

    val semaforo_color = command.registro.SOJ_OTROS_ATRIBUTOS match {
      case Some(r) => r.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR.getOrElse("")
      case None => ""
    }

    def isTipo(cmd: ObjetoUpdateFromTri) = {

      val result = DMNTreintaPorcientoTipo.calcularDmn(
        DmnObjeto(
          cmd.registro.SOJ_TIPO_OBJETO,
          cmd.registro.SOJ_ADHERIDO_DEBITO.getOrElse(""),
          cmd.registro.SOJ_ESTADO.getOrElse(""),
          cmd.registro.SOJ_TITULARIDAD.getOrElse(""),
          semaforo_color,
          cmd.registro.SOJ_TIPO_EXCLUSION.getOrElse(""),
          ""
        )
      )

      result match {
        case f if f.equals(2) =>
          ("2", 2)
        case f if f.equals(-1) =>
          ("1", -1)

        case f if f.equals(1) =>
          ("1", 1)

      }
    }

    val dmn = isTipo(command)

//    def getCCParams(evento: ObjetoExternalDto, estado: ObjetoExternalDto) = {
//      val declaredFields = evento.getClass.getDeclaredFields
//      var objetoNuevoTest = evento
//
//      declaredFields.foreach { campo =>
//        val campoEvento = objetoNuevoTest.getClass.getDeclaredField(campo.getName)
//        val campoEstado = estado.getClass.getDeclaredField(campo.getName)
//        campoEvento.setAccessible(true)
//        campoEstado.setAccessible(true)
//
//        if (campoEvento.getName == "SOJ_OTROS_ATRIBUTOS") {
//          val otrosAtributosEventoOpt = campoEvento.get(objetoNuevoTest).asInstanceOf[DetallesObjeto]
//          val otrosAtributosEstadoOpt = campoEstado.get(estado).asInstanceOf[DetallesObjeto]
//
//          val atributosActualizados: DetallesObjeto = actualizarDetalles(otrosAtributosEventoOpt, otrosAtributosEstadoOpt)
//          val otrosAtributosActualizados: Some[ListDetallesObjeto] = Some(ListDetallesObjeto(List(atributosActualizados)))
//          campoEvento.set(objetoNuevoTest, otrosAtributosActualizados)
//        }
//
//        if (campoEvento.get(evento) == None) {
//          campoEvento.set(objetoNuevoTest, campoEstado.get(estado))
//        } else if (campoEvento.get(evento).equals(Some("null"))) {
//          campoEvento.set(objetoNuevoTest, None)
//        }
//      }
//      objetoNuevoTest
//    }
//
//    def actualizarDetalles(atributosEvento: DetallesObjeto, atributosEstado: DetallesObjeto) = {
//      val declaredFields = atributosEvento.getClass.getDeclaredFields
//      var atributosNuevo = atributosEvento
//
//      declaredFields.foreach{ campo =>
//        val campoEvento = atributosNuevo.getClass.getDeclaredField(campo.getName)
//        val campoEstado = atributosEstado.getClass.getDeclaredField(campo.getName)
//        campoEvento.setAccessible(true)
//        campoEstado.setAccessible(true)
//
//        if(campoEvento.get(atributosEvento) == None){
//          campoEvento.set(atributosNuevo, campoEstado.get(atributosEstado))
//        } else if(campoEvento.get(atributosEvento).equals(Some("null"))){
//          campoEvento.set(atributosNuevo, None)
//        }
//      }
//      atributosNuevo
//    }

    def getCCParams(evento: ObjetoExternalDto, estado: ObjetoExternalDto) = {
      val declaredFields = evento.getClass.getDeclaredFields
      var objetoNuevoTest = evento

      declaredFields.foreach { campo =>
        val campoEvento = objetoNuevoTest.getClass.getDeclaredField(campo.getName)
        val campoEstado = estado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)

        if (campoEvento.getName == "SOJ_OTROS_ATRIBUTOS") {
          val otrosAtributosEventoOpt = campoEvento.get(objetoNuevoTest).asInstanceOf[Option[ListDetallesObjeto]]
          val otrosAtributosEstadoOpt = campoEstado.get(estado).asInstanceOf[Option[ListDetallesObjeto]]

          val atributosActualizados = actualizarOtrosAtributos(otrosAtributosEventoOpt, otrosAtributosEstadoOpt)
          campoEvento.set(objetoNuevoTest, atributosActualizados)
        } else if (campoEvento.get(objetoNuevoTest) == None) {
          campoEvento.set(objetoNuevoTest, campoEstado.get(estado))
        } else if (campoEvento.get(objetoNuevoTest).equals(Some("null"))) {
          campoEvento.set(objetoNuevoTest, None)
        }
      }
      objetoNuevoTest
    }

    def actualizarOtrosAtributos(atributosEvento: Option[ListDetallesObjeto], atributosEstado: Option[ListDetallesObjeto]): Option[ListDetallesObjeto] = {
      (atributosEvento, atributosEstado) match {
        case (None, estado) => estado
        case (Some(ListDetallesObjeto(Nil)), _) => None
        case (Some(ListDetallesObjeto(detallesEvento)), Some(ListDetallesObjeto(detallesEstado))) =>
          Some(ListDetallesObjeto(
            detallesEvento.map { detalleEvento =>
              actualizarDetalle(detalleEvento, detallesEstado.headOption.getOrElse(detalleEvento))
            }
          ))
        case (evento, _) => evento
      }
    }

    def actualizarDetalle(detalleEvento: DetallesObjeto, detalleEstado: DetallesObjeto): DetallesObjeto = {
      val declaredFields = detalleEvento.getClass.getDeclaredFields
      var detalleNuevo = detalleEvento

      declaredFields.foreach { campo =>
        campo.setAccessible(true)
        val valorEvento = campo.get(detalleEvento)
        val valorEstado = campo.get(detalleEstado)

        if (valorEvento == None) {
          campo.set(detalleNuevo, valorEstado)
        } else if (valorEvento.equals(Some("null"))) {
          campo.set(detalleNuevo, None)
        }
      }
      detalleNuevo
    }


        //TODO Validate the first event, with no state, enters in the case None.
    def getObjetoFFF() = {
      val objetoFFF = actor.state.registro match {
        case None => command.registro
        case Some(value) => getCCParams(command.registro, value)
      }
      objetoFFF
    }

    val event = ObjetoEvents.ObjetoUpdatedFromTri(
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      getObjetoFFF(),
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito,
      Some(dmn._1),
      Some(dmn._2)
    )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      persistSnapshotEvent(event, actor, command, requeriment)
    }
  }

}

object test {
  def persistSnapshotEvent(event: ObjetoUpdatedFromTri,
                           actor: ObjetoActor,
                           command: ObjetoUpdateFromTri,
                           requeriment: MonitoringAndMessageProducer): Success[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(this.getClass)

    implicit val ac: ActorSystem = actor.context.system
    val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    actor.persistEvent(event) { () =>
      actor.state += event

      //todo juicio persiste, pero no se us apara el calculo del 30%?

      if (actor.state.registro.get.SOJ_TIPO_OBJETO
        .equals("M")) { // todo tipo M , pero si para el calculo de deuda para un sujeto. Objeto juicio queda atado a cuit, pero no se va a teber en cuanta cuando se calcule el 30%, no se guarda el vinculo.
        if (actor.state.tiene30Objeto.equals(false)) {
          val res = actor.context.parent.ask[Response.SuccessProcessing](
            SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento(
              command.deliveryId,
              command.sujetoId,
              command.objetoId,
              command.tipoObjeto,
              actor.state.saldo,
              actor.state.obligacionesSaldo.values.sum,
              actor.state.clasificacionObjeto
            )
          )
          res.onComplete {
            case Failure(exception) =>
              log.error(
                "Error to send event to sujeto tipo M false " + exception + " objID: " + command.objetoId + "sujID: " + command.sujetoId
              )
            case Success(value) =>
              log.debug(
                "Sent event to sujeto tipo M false " + " objID: " + command.objetoId + " sujID: " + command.sujetoId
              )
          }
        } else {
          {
            val res = actor.context.parent.ask[Response.SuccessProcessing](
              SujetoCommands.SujetoUpdateFromObjeto(
                command.deliveryId,
                command.sujetoId,
                command.objetoId,
                command.tipoObjeto,
                actor.state.saldo,
                actor.state.obligacionesSaldo.values.sum,
                actor.state.clasificacionObjeto
              )
            )
            res.onComplete {
              case Failure(exception) =>
                log.error(
                  "Error to send event to sujeto tipo M true " + exception + " objID: " + command.objetoId + "sujID: " + command.sujetoId
                )
              case Success(value) =>
                log.debug(
                  "Sent event to sujeto tipo M true " + " objID: " + command.objetoId + " sujID: " + command.sujetoId
                )
            }
          }
        }
        actor.persistSnapshot(event, actor.state) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      } else {
        SendObjetoToObjetoVinculo(Obje,
          actor,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          command.registro.SOJ_ESTADO,
          requeriment)
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
}
