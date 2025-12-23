package consumers.no_registral.sujeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands. ObjetoUpdateFromSujeto
import consumers. no_registral.sujeto.domain.SujetoState
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

    // ✅ Usar el último deliveryId recibido desde un objeto
    val deliveryIdToUse = if (currentState.ultimoDeliveryIdRecibido > 0) {
      currentState.ultimoDeliveryIdRecibido
    } else {
      currentState.lastDeliveryIdByEvents
    }

    if (currentState.diffStates) {
      val childName = s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto"

      actorContext.child(childName) match {
        case Some(objChild) =>
          log.info(s"[SEND-TO-OBJETO-SPECIFIC-FOUND] Hijo encontrado: ${objChild.path}")

          objChild.ask[Response.SuccessProcessing](
            ObjetoUpdateFromSujeto(
              deliveryId = deliveryIdToUse,
              sujetoId = sujetoId,
              objetoId = objetoId,
              tipoObjeto = tipoObjeto,
              tiene30Sujeto = currentState.tiene30Sujeto,
              exclusionSUjeto = currentState.exclusionSujeto,
              currentState. dmnDescripcion
            )
          )

        case _ =>
          log.warn(s"[SEND-TO-OBJETO-SPECIFIC-NOT-FOUND] Hijo NO encontrado: $childName")
      }
    } else {
      // ✅ Caso broadcast: Enviar a TODOS los ObjetoActor hijos (NO a ObligacionActor)
      val allChildren = actorContext.children.toSeq
      val objetoActors = allChildren.filter(_.path.toString.contains("-Objeto-"))

      log.info(s"[SEND-TO-OBJETO-BROADCAST] Total hijos:  ${allChildren.size}, ObjetoActors: ${objetoActors.size}")

      if (objetoActors.isEmpty) {
        log.warn(s"[SEND-TO-OBJETO-BROADCAST-EMPTY] El SujetoActor NO tiene ObjetoActor hijos.  sujetoId=$sujetoId")
      }

      objetoActors.foreach(actor => {
        val actorPath = actor.path.toString

        log.info(s"[SEND-TO-OBJETO-BROADCAST-CHILD] Procesando ObjetoActor: $actorPath")

        // ✅ Extraer objetoId del path con regex
        val objetoIdExtracted = """Objeto-(.*?)-""".r.findFirstMatchIn(actorPath) match {
          case Some(matched) =>
            val extracted = matched.group(1)
            log.info(s"[SEND-TO-OBJETO-BROADCAST-REGEX-OK] objetoId extraído: '$extracted'")
            extracted

          case None =>
            log. warn(s"[SEND-TO-OBJETO-BROADCAST-REGEX-FAIL] NO se pudo extraer objetoId del path:  $actorPath")
            ""
        }

        val tipoObjetoExtracted = actorPath.last.toString

        if (objetoIdExtracted.nonEmpty) {
          log.info(s"[SEND-TO-OBJETO-BROADCAST-SEND] Enviando comando - objetoId='$objetoIdExtracted', tipoObjeto='$tipoObjetoExtracted', deliveryId=$deliveryIdToUse")

          actor.ask[Response.SuccessProcessing](
            ObjetoUpdateFromSujeto(
              deliveryIdToUse,
              sujetoId,
              objetoIdExtracted,
              tipoObjetoExtracted,
              currentState.tiene30Sujeto,
              currentState.exclusionSujeto,
              currentState.dmnDescripcion
            )
          )
        } else {
          log.error(s"[SEND-TO-OBJETO-BROADCAST-SKIP] objetoId VACÍO - NO se enviará comando.  Path: $actorPath")
        }
      })

      log.info(s"[SEND-TO-OBJETO-BROADCAST-COMPLETE] Terminó el broadcast a ${objetoActors.size} ObjetoActors")
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

    // ✅ Usar el último deliveryId recibido desde un objeto
    val deliveryIdToUse = if (currentState.ultimoDeliveryIdRecibido > 0) {
      currentState.ultimoDeliveryIdRecibido
    } else {
      currentState.lastDeliveryIdByEvents
    }

    val allChildren = actorContext.children. toSeq
    val objetoActors = allChildren.filter(_.path.toString.contains("-Objeto-"))


    objetoActors.foreach(actor => {
      val actorPath = actor.path.toString

      val objetoIdExtracted = """Objeto-(.*?)-""".r.findFirstMatchIn(actorPath) match {
        case Some(matched) =>
          val extracted = matched.group(1)
          extracted

        case None =>
          log.warn(s"[SEND-TO-OBJETO-FROM-SUJETO-REGEX-FAIL] NO se pudo extraer objetoId del path:  $actorPath")
          ""
      }

      val tipoObjetoExtracted = actorPath.last.toString

      if (objetoIdExtracted.nonEmpty) {
        actor.ask[Response. SuccessProcessing](
          ObjetoUpdateFromSujeto(
            deliveryIdToUse,
            sujetoId,
            objetoIdExtracted,
            tipoObjetoExtracted,
            currentState. tiene30Sujeto,
            exclusionSujeto,
            currentState.dmnDescripcion
          )
        )
      } else {
        log.error(s"[SEND-TO-OBJETO-FROM-SUJETO-SKIP] objetoId VACÍO - NO se enviará comando. Path: $actorPath")
      }
    })
    log.info(s"[SEND-TO-OBJETO-FROM-SUJETO-COMPLETE] Terminó el envío a ${objetoActors. size} ObjetoActors")
  }
}