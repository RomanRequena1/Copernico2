package consumers.no_registral.sujeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.sujeto.domain.SujetoState
import design_principles.actor_model.Response
import org.slf4j.{Logger, LoggerFactory}

object SendToObjeto {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(currentState: SujetoState,
            sender: ActorRef,
            actorContext: ActorContext,
            sujetoId: String,
            objetoId: String,
            tipoObjeto: String): Unit = {

    if (currentState.diffStates) {
      val childName = s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto"

      actorContext.child(childName) match {
        case Some(objChild) =>
          objChild.ask[Response.SuccessProcessing](
            ObjetoUpdateFromSujeto(
              deliveryId = currentState.lastDeliveryIdByEvents,
              sujetoId = sujetoId,
              objetoId = objetoId,
              tipoObjeto = tipoObjeto,
              tiene30Sujeto = currentState.tiene30Sujeto,
              exclusionSUjeto = currentState.exclusionSujeto,
              currentState.dmnDescripcion,
              currentState.idExterno,
              currentState.deliveryIdObligacion
            )
          )

        case _ =>
          log.warn(s"[SEND-TO-OBJETO-SPECIFIC-NOT-FOUND] Hijo NO encontrado: $childName")
      }
    } else {
      val allChildren = actorContext.children.toSeq
      val objetoActors = allChildren.filter(_.path.toString.contains("-Objeto-"))

      log.info(s"[SEND-TO-OBJETO-BROADCAST] Total hijos: ${allChildren.size}, ObjetoActors: ${objetoActors.size}")

      if (objetoActors.isEmpty) {
        log.warn(s"[SEND-TO-OBJETO-BROADCAST-EMPTY] El SujetoActor NO tiene ObjetoActor hijos. sujetoId=$sujetoId")
      }

      objetoActors.foreach(actor => {
        val actorPath = actor.path.toString

        val objetoIdExtracted = """Objeto-(.*?)-""".r.findFirstMatchIn(actorPath) match {
          case Some(matched) => matched.group(1)
          case None =>
            log.warn(s"[SEND-TO-OBJETO-BROADCAST-REGEX-FAIL] NO se pudo extraer objetoId del path: $actorPath")
            ""
        }

        val tipoObjetoExtracted = actorPath.last.toString

        if (objetoIdExtracted.nonEmpty) {
          actor.ask[Response.SuccessProcessing](
            ObjetoUpdateFromSujeto(
              deliveryId = currentState.lastDeliveryIdByEvents,
              sujetoId,
              objetoIdExtracted,
              tipoObjetoExtracted,
              currentState.tiene30Sujeto,
              currentState.exclusionSujeto,
              currentState.dmnDescripcion,
              currentState.idExterno,
              currentState.deliveryIdObligacion
            )
          )
        }
      })
    }
  }
}

object SendToObjetoFromSujeto {
  private val log: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(currentState: SujetoState,
            sender: ActorRef,
            actorContext: ActorContext,
            sujetoId: String,
            exclusionSujeto: Option[String]): Unit = {

    val allChildren = actorContext.children.toSeq
    val objetoActors = allChildren.filter(_.path.toString.contains("-Objeto-"))

    objetoActors.foreach(actor => {
      val actorPath = actor.path.toString

      val objetoIdExtracted = """Objeto-(.*?)-""".r.findFirstMatchIn(actorPath) match {
        case Some(matched) => matched.group(1)
        case None =>
          log.warn(s"[SEND-TO-OBJETO-FROM-SUJETO-REGEX-FAIL] NO se pudo extraer objetoId del path: $actorPath")
          ""
      }

      val tipoObjetoExtracted = actorPath.last.toString

      if (objetoIdExtracted.nonEmpty) {
        actor.ask[Response.SuccessProcessing](
          ObjetoUpdateFromSujeto(
            currentState.lastDeliveryIdByEvents,
            sujetoId,
            objetoIdExtracted,
            tipoObjetoExtracted,
            currentState.tiene30Sujeto,
            exclusionSujeto,
            currentState.dmnDescripcion,
            currentState.idExterno,
            currentState.deliveryIdObligacion
          )
        )
      }
    })
  }
}