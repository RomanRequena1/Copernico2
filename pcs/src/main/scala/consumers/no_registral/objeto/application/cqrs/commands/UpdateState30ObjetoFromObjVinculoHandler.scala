package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.ActorRef
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral. objeto.application.helper.{SendToSujeto, SendToSujeto1}
import consumers. no_registral.objeto.domain.ObjetoEvents.{DmnResumen, UpdatedState30ObjetoFromObjVinculo}
import consumers.no_registral. objeto.infrastructure.dependency_injection. ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped. command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles. actor_model.Response

import scala.util.{Success, Try}

class UpdateState30ObjetoFromObjVinculoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.UpdateState30ObjetoFromObjVinculo] {

  private val resumenEnabled:  Option[String] = Option(System.getenv("KAFKA_BROKERS_LIST_PSRM"))

  override def handle(
                       command: ObjetoCommands. UpdateState30ObjetoFromObjVinculo
                     ): Try[Response.SuccessProcessing] = {
    log. debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path. toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val sender = actor. context.sender()

    val event = UpdatedState30ObjetoFromObjVinculo(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command. objetoId,
      command. tipoObjeto,
      command.tiene30ObjetoVinculo,
      command.exclusionObjetoVinculo
    )

    actor.persistEvent(event) { () =>
      actor.state += event
      actor.persistSnapshot(event, actor. state) { () =>

        val tiene30ObjetoFinal = if (!command.tiene30ObjetoVinculo && actor.state.obligaciones.isEmpty) {
          false
        } else if (command.tiene30ObjetoVinculo && actor.state.obligaciones.isEmpty) {
          true
        } else {
          actor.state.tiene30Objeto
        }

        actor.state = actor.state.copy(tiene30Objeto = tiene30ObjetoFinal)

        def esTipoObjetoPermitido(tipo: String): Boolean = Set("A", "I", "N").contains(tipo)

        if (actor.state.tiene30Sujeto. isDefined &&
          esTipoObjetoPermitido(command.tipoObjeto) &&
          resumenEnabled.isDefined) {

          // Calcular el DMN final
          val result:  Boolean = DMNTreintaPorcientoFinal.calcularDmnFinal(
            DmnFinal(
              command.exclusionObjetoVinculo,
              actor.state.exclusionObjeto,
              actor.state.clasificacionObjeto,
              tiene30ObjetoFinal,
              actor.state.tiene30Sujeto.get,
              command.tiene30ObjetoVinculo
            )
          )

          val aplicarDescuento = if (result) Some(true) else Some(false)

          val (dmnNumeroParaPSRM, dmnDescripcionParaPSRM) = {
            if (actor.state. dmnNumero.isEmpty &&
              aplicarDescuento.contains(false) &&
              actor.state. tiene30Objeto) {

              (Some(99), Some("No cumple por deuda en otro objeto del sujeto"))

            } else {
              (actor.state.dmnNumero, actor.state.dmnDescripcion)
            }
          }

          val eventDmn = DmnResumen(
            if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
            command.sujetoId,
            command. objetoId,
            command. tipoObjeto,
            actor.state.idExterno,
            Some(actor.state.fechaUltMod),
            aplicarDescuento,
            dmnNumeroParaPSRM,
            dmnDescripcionParaPSRM
          )

          // Enviar al ObjetoVinculoActor para auditoría
          implicit val system = actor.context.system
          val vinculoActor:  ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

          log.info(s"[PAGO-AUDITORIA] Enviando auditoría desde UpdateState30ObjetoFromObjVinculo - objetoId=${command.objetoId}, aplicarDescuento=$aplicarDescuento")

          vinculoActor !  ObjetoVinculoCommands.AuditarYEnviarResumen(
            deliveryId = command.deliveryId,
            objetoId = command.objetoId,
            tipoObj = command.tipoObjeto,
            sujetoId = command.sujetoId,
            aplicarDescuento = aplicarDescuento,
            eventDmn = eventDmn
          )
        }

        actor.persistSnapshot(event, actor.state) { () =>
          if (tiene30ObjetoFinal) {
            SendToSujeto1(actor, requeriment, event)
          } else {
            SendToSujeto(actor, requeriment, event)
          }
          sender ! Response. SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }

      if (actor.state.eventCounter == eventCounterMax) {
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
    }
    Success(Response. SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}