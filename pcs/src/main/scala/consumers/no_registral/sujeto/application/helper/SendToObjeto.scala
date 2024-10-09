package consumers.no_registral.sujeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.sujeto.domain.SujetoState
import design_principles.actor_model.Response

object SendToObjeto {
  def apply(currentState: SujetoState,
            sender: ActorRef,
            actorContext: ActorContext,
            sujetoId: String,
            objetoId: String,
            tipoObjeto: String): Unit = {
    if (currentState.diffStates) {
      actorContext
        .child(s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto") match {
        case Some(objChild) =>
          objChild.ask[Response.SuccessProcessing](
            ObjetoUpdateFromSujeto(
              deliveryId = currentState.lastDeliveryIdByEvents,
              sujetoId = sujetoId,
              objetoId = objetoId,
              tipoObjeto = tipoObjeto,
              tiene30Sujeto = currentState.tiene30Sujeto,
              exclusionSUjeto = currentState.exclusionSujeto
            )
          )
        case _ => ()
      }
    } else {

      actorContext.children.foreach(actor => {
        actor.ask[Response.SuccessProcessing](
          ObjetoUpdateFromSujeto(
            currentState.lastDeliveryIdByEvents,
            sujetoId,
            """Objeto-(.*?)-""".r.findFirstMatchIn(actor.path.toString) match {
              case Some(matched) => matched.group(1)
              case None => ""
            },
            actor.path.toString.last.toString,
            currentState.tiene30Sujeto,
            currentState.exclusionSujeto,

          )
        )
      })
    }
  }
}

object SendToObjetoFromSujeto {
  def apply(currentState: SujetoState,
            sender: ActorRef,
            actorContext: ActorContext,
            sujetoId: String,
            exclusionSujeto: Option[String]): Unit = {

    actorContext.children.foreach(actor => {
      actor.ask[Response.SuccessProcessing](
        ObjetoUpdateFromSujeto(
          currentState.lastDeliveryIdByEvents,
          sujetoId,
          """Objeto-(.*?)-""".r.findFirstMatchIn(actor.path.toString) match {
            case Some(matched) => matched.group(1)
            case None => ""
          },
          actor.path.toString.last.toString,
          currentState.tiene30Sujeto,
          exclusionSujeto
        )
      )
    })
  }
}
