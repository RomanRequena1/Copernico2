package consumers.no_registral.sujeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.sujeto.domain.SujetoEvents.{SujetoBajaFromObjetoSet, SujetoUpdatedFromObjeto, SujetoUpdatedFromObjetoTreintaPorciento}
import consumers.no_registral.sujeto.domain.SujetoState

object SendToObjeto {
  def apply(currentState: SujetoState, sender: ActorRef, actorContext: ActorContext, sujetoId: String) : Unit = {

    val isExclusionSujeto = QueryExclusionSujeto(sujetoId)
      if (currentState.diffStates) {
        sender ! ObjetoUpdateFromSujeto(
          currentState.lastDeliveryIdByEvents,
          sujetoId,
          """Objeto-(.*?)-""".r.findFirstMatchIn(sender.path.toString) match {
            case Some(matched) => matched.group(1)
            case None => ""
          },
          sender.path.toString.last.toString,
          currentState.tiene30Sujeto,
          if(isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head
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
            if(isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head

          )
        })
      }
  }



}
