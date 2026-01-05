package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.objeto.application.cqrs.commands.test.persistSnapshotEvent
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo.DmnObjeto
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetoExternalDto}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.{DetallesObjeto, ListDetallesObjeto}
import consumers.no_registral.objeto.application.helper.{SendObjetoToObjetoVinculo, StateParcialObjeto}
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

import java.time.LocalDateTime
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

    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
      case None => None
    }

    val semaforo_color = command.registro.SOJ_OTROS_ATRIBUTOS match {
      case Some(r) => r.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR.getOrElse("")
      case None => ""
    }

    if (!StateParcialObjeto.shouldProcessEvent(command.registro, actor.state.registro)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- rechazando evento de state parcial del semáforo/VDO porque el vínculo no existe | sujetoId: ${command.sujetoId}, objetoId: ${command.objetoId}"
      )
      sender ! Response.SuccessProcessing(s"VINCULO_INEXISTENTE-${command.aggregateRoot}", command.deliveryId)

      return Success(Response.SuccessProcessing(s"VINCULO_INEXISTENTE-${command.aggregateRoot}", command.deliveryId))
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

    def getCCParams(evento: ObjetoExternalDto, estado: ObjetoExternalDto): ObjetoExternalDto = {
      StateParcialObjeto.stateParcialCC(evento, Some(estado))
    }

    def getObjetoFFF() = {
      val objetoFFF = actor.state.registro match {
        case None => StateParcialObjeto.stateParcialCC(command.registro, None)
        case Some(value) => getCCParams(command.registro, value)
      }
      objetoFFF
    }

    val clasificacionObj = isTipo(command)

    val stateParcialEnabled: String = Option(System.getenv("STATE_PARCIAL_OBJETO_TRI")).getOrElse("OFF")

    val event = ObjetoEvents.ObjetoUpdatedFromTri(
      //TODO: validar para que esta este If
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      stateParcialEnabled.equals("ON") match {
        case true => getObjetoFFF()
        case false => command.registro
      },
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito,
      Some(clasificacionObj._1),
      Some(clasificacionObj._2)
    )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
      sender ! Response.SuccessProcessing("IDEM-" + command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      persistSnapshotEvent(event, actor, command, requeriment)
    }
  }
}

object test {
  def actualizarObjeto(evento: ObjetoExternalDto.ObjetosTri, estado: ObjetoExternalDto.ObjetosTri) = {
    var objetoFinal = evento

    if (evento.SOJ_SUBTIPO.isDefined) {
      objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = evento.SOJ_SUBTIPO)
    } else {
      objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = estado.SOJ_SUBTIPO)
    }

    if (evento.SOJ_ADHERIDO_DEBITO.isDefined) {
      objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = evento.SOJ_ADHERIDO_DEBITO)
    } else {
      objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = estado.SOJ_ADHERIDO_DEBITO)
    }

    objetoFinal
  }

  def persistSnapshotEvent(event: ObjetoUpdatedFromTri,
                           actor: ObjetoActor,
                           command: ObjetoUpdateFromTri,
                           requeriment: MonitoringAndMessageProducer): Success[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(this.getClass)

    if (!StateParcialObjeto.shouldProcessEvent(command.registro, actor.state.registro)) {
      log.warn(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- rechazando evento de state parcial en persistSnapshotEvent porque el vínculo no existe | sujetoId: ${command.sujetoId}, objetoId: ${command.objetoId}"
      )
      return Success(Response.SuccessProcessing(s"VINCULO_INEXISTENTE-${command.aggregateRoot}", command.deliveryId))
    }

    implicit val ac: ActorSystem = actor.context.system
    val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    actor.persistEvent(event) { () =>
      actor.state += event

      if (actor.state.registro.get.SOJ_TIPO_OBJETO.equals("M")) {
        if (actor.state.tiene30Objeto.equals(false)) {
          val res = actor.context.parent.ask[Response.SuccessProcessing](
            SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento(
              command.deliveryId,
              command.sujetoId,
              command.objetoId,
              command.tipoObjeto,
              actor.state.saldo,
              actor.state.obligacionesSaldo.values.sum,
              actor.state.clasificacionObjeto,
              Some(command.deliveryId)
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
                actor.state.clasificacionObjeto,
                Some(command.deliveryId)
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
        actor.persistSnapshot(event, actor.state) { () =>
          SendObjetoToObjetoVinculo(
            Obje,
            actor,
            command.sujetoId,
            command.objetoId,
            command.tipoObjeto,
            command.registro.SOJ_ESTADO,
            requeriment,
            command
          )
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 2))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}