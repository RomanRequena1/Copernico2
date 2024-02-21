package consumers.no_registral.sujeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.sujeto.domain.SujetoEvents.{SujetoBajaFromObjetoSet, SujetoUpdatedFromObjeto, SujetoUpdatedFromObjetoTreintaPorciento}
import consumers.no_registral.sujeto.domain.SujetoState

object SendToObjeto {
  def apply(currentState: SujetoState, sender: ActorRef, actorContext: ActorContext, sujetoId: String, objetoId: String, tipoObjeto: String) : Unit = {
    println("CUMBIAAAAAAAAAAAAAAAA PATH "+  sender.path.toString)
    //val isExclusionSujeto = QueryExclusionSujeto(sujetoId)
      if (currentState.diffStates) {
        sender ! ObjetoUpdateFromSujeto(
          currentState.lastDeliveryIdByEvents,
          sujetoId,
          objetoId,
          tipoObjeto,
          currentState.tiene30Sujeto,
          ""
        )
      } else{
        actorContext.children.foreach( actor => {
          val actorSelection = actorContext.actorSelection(actor.path)
          actorSelection ! ObjetoUpdateFromSujeto(
            currentState.lastDeliveryIdByEvents,
            sujetoId,
            """Objeto-(.*?)-""".r.findFirstMatchIn(actor.path.toString) match {
              case Some(matched) => matched.group(1)
              case None => ""
            },
            actor.path.toString.last.toString,
            currentState.tiene30Sujeto,
            ""

          )
        })
      }
  }



}
