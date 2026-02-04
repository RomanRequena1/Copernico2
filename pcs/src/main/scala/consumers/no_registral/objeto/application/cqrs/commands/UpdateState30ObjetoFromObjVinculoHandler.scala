package consumers.no_registral.objeto.application.cqrs.commands

import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.helper.{SendToSujeto, SendToSujeto1}
import consumers.no_registral.objeto.domain.ObjetoEvents.UpdatedState30ObjetoFromObjVinculo
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class UpdateState30ObjetoFromObjVinculoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.UpdateState30ObjetoFromObjVinculo] {

  override def handle(
                       command: ObjetoCommands.UpdateState30ObjetoFromObjVinculo
                     ): Try[Response.SuccessProcessing] = {
    log.debug(
      f"""|CUMBIA
          |  | command_id: ${command.deliveryId}%-20s | state_id: ${actor.state.lastDeliveryIdByEvents}%-5s
          |  | sender    : ${actor.context.sender().path.toString.replace("akka://PersonClassificationService", "")}
          |  | self      : ${actor.self.path.toString.replace("akka://PersonClassificationService", "")}
          |""".stripMargin
    )
    val sender = actor.context.sender()

    val event = UpdatedState30ObjetoFromObjVinculo(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30ObjetoVinculo,
      command.exclusionObjetoVinculo,
    )

    // LOG: Estado inicial antes de procesar
    println(
      s"[OBJETO-FROM-VINCULO-INIT] objetoId=${command.objetoId}, sujetoId=${command.sujetoId} - " +
        s"command.tiene30ObjetoVinculo=${command.tiene30ObjetoVinculo}, " +
        s"actor.state.obligaciones.size=${actor.state.obligaciones.size}, " +
        s"actor.state.obligaciones.isEmpty=${actor.state.obligaciones.isEmpty}, " +
        s"actor.state.tiene30Objeto=${actor.state.tiene30Objeto}, " +
        s"actor.state.tiene30ObjetoVinculo=${actor.state.tiene30ObjetoVinculo}"
    )

    actor.persistEvent(event) { () =>
      println("EV ID4: " + command.deliveryId)
      actor.state += event
      actor.persistSnapshot(event, actor.state) { () =>

        val tiene30ObjetoFinal = if (!command.tiene30ObjetoVinculo && actor.state.obligaciones.isEmpty) {
          // LOG: Caso sin obligaciones y vínculo false
          println(
            s"[OBJETO-FROM-VINCULO-CALC] objetoId=${command.objetoId} - " +
              s"CASO: !tiene30ObjetoVinculo && obligaciones.isEmpty -> false (PROBLEMA POTENCIAL)"
          )
          false
        } else if (command.tiene30ObjetoVinculo && actor.state.obligaciones.isEmpty) {
          // LOG: Caso sin obligaciones y vínculo true
          println(
            s"[OBJETO-FROM-VINCULO-CALC] objetoId=${command.objetoId} - " +
              s"CASO: tiene30ObjetoVinculo && obligaciones.isEmpty -> true"
          )
          true
        } else {
          // LOG: Caso con obligaciones
          println(
            s"[OBJETO-FROM-VINCULO-CALC] objetoId=${command.objetoId} - " +
              s"CASO: tiene obligaciones -> usando actor.state.tiene30Objeto=${actor.state.tiene30Objeto}"
          )
          actor.state.tiene30Objeto
        }

        actor.state = actor.state.copy(tiene30Objeto = tiene30ObjetoFinal)

        // CORRECCION: Lógica de DMN consistente con tiene30ObjetoFinal
        // Si tiene30ObjetoFinal=false, NO puede tener dmnNumero=1 ("No Deuda")
        val (dmnNumeroFinal, dmnDescripcionFinal) = {
          if (tiene30ObjetoFinal) {
            // Objeto cumple el 30% - puede heredar DMN del estado o del comando
            if (actor.state.dmnNumero.isDefined) {
              (actor.state.dmnNumero, actor.state.dmnDescripcion)
            } else if (command.dmnNumero.isDefined) {
              (command.dmnNumero, command.dmnDescripcion)
            } else {
              (None, None)
            }
          } else {
            // Objeto NO cumple el 30% - verificar consistencia
            if (actor.state.dmnNumero.isDefined && !actor.state.dmnNumero.contains(1)) {
              // Tiene DMN definido y NO es "No Deuda" (1) - mantener
              (actor.state.dmnNumero, actor.state.dmnDescripcion)
            } else if (command.dmnNumero.isDefined && !command.dmnNumero.contains(1)) {
              // Comando trae DMN y NO es "No Deuda" (1) - usar del comando
              (command.dmnNumero, command.dmnDescripcion)
            } else {
              // No hay DMN válido o era "No Deuda" - limpiar para que se calcule después
              (None, None)
            }
          }
        }

        actor.state = actor.state.copy(
          dmnNumero = dmnNumeroFinal,
          dmnDescripcion = dmnDescripcionFinal
        )

        // LOG: Estado final y decisión de envío al sujeto
        println(
          s"[OBJETO-FROM-VINCULO-FINAL] objetoId=${command.objetoId}, sujetoId=${command.sujetoId} - " +
            s"tiene30ObjetoFinal=$tiene30ObjetoFinal, " +
            s"dmnNumero=$dmnNumeroFinal, dmnDescripcion=$dmnDescripcionFinal, " +
            s"ENVIANDO_A_SUJETO=${if (tiene30ObjetoFinal) "SendToSujeto1 (sin deuda)" else "SendToSujeto (con deuda)"}"
        )

        actor.persistSnapshot(event, actor.state) { () =>
          if (tiene30ObjetoFinal) {
            SendToSujeto1(actor, requeriment, event, Some(command.deliveryId), command.idExterno)
          } else {
            SendToSujeto(actor, requeriment, event, Some(command.deliveryId), command.idExterno)
          }
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }

      if (actor.state.eventCounter == eventCounterMax) {
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}