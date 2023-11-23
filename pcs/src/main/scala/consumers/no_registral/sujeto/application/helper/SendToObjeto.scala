package consumers.no_registral.sujeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.sujeto.domain.SujetoEvents.{SujetoBajaFromObjetoSet, SujetoUpdatedFromObjeto, SujetoUpdatedFromObjetoTreintaPorciento}
import consumers.no_registral.sujeto.domain.SujetoState

object SendToObjeto {
  def apply(currentState: SujetoState, sender: ActorRef,  children: Iterable[ActorRef], actorContext: ActorContext, event: SujetoUpdatedFromObjeto) : Unit = {

    val isExclusionSujeto = QueryExclusionSujeto(event.sujetoId)
    println("DIFERENCIA -> " + currentState.diffStates)
      if (currentState.diffStates) {
        println("TREINTA 11 " +currentState)
        sender ! ObjetoUpdateFromSujeto(
          currentState.lastDeliveryIdByEvents,
          event.sujetoId,
          """Objeto-(.*?)-""".r.findFirstMatchIn(sender.path.toString) match {
            case Some(matched) => matched.group(1)
            case None => ""
          },
          sender.path.toString.last.toString,
          currentState.deuda30Sujeto,
          if(isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head
        )
      } else{
        children.foreach( actor => {
          val actorSelection = actorContext.actorSelection(actor.path)
          actorSelection ! ObjetoUpdateFromSujeto(
            currentState.lastDeliveryIdByEvents,
            event.sujetoId,
            """Objeto-(.*?)-""".r.findFirstMatchIn(actor.path.toString) match {
              case Some(matched) => matched.group(1)
              case None => ""
            },
            actor.path.toString.last.toString,
            currentState.deuda30Sujeto,
            if(isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head

          )
        })
      }
  }

  def apply(currentState: SujetoState, sender: ActorRef, children: Iterable[ActorRef], actorContext: ActorContext, event: SujetoUpdatedFromObjetoTreintaPorciento): Unit = {

    val isExclusionSujeto = QueryExclusionSujeto(event.sujetoId)
    println("DIFERENCIA -> " + currentState.diffStates)
    if (currentState.diffStates) {
      println("TREINTA 11.1 " + currentState)
      sender ! ObjetoUpdateFromSujeto(
        currentState.lastDeliveryIdByEvents,
        event.sujetoId,
        """Objeto-(.*?)-""".r.findFirstMatchIn(sender.path.toString) match {
          case Some(matched) => matched.group(1)
          case None => ""
        },
        sender.path.toString.last.toString,
        currentState.deuda30Sujeto,
        if (isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head
      )
    } else {
      children.foreach(actor => {
        val actorSelection = actorContext.actorSelection(actor.path)
        actorSelection ! ObjetoUpdateFromSujeto(
          currentState.lastDeliveryIdByEvents,
          event.sujetoId,
          """Objeto-(.*?)-""".r.findFirstMatchIn(actor.path.toString) match {
            case Some(matched) => matched.group(1)
            case None => ""
          },
          actor.path.toString.last.toString,
          currentState.deuda30Sujeto,
          if (isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head

        )
      })
    }
  }

  def apply(currentState: SujetoState, sender: ActorRef, children: Iterable[ActorRef], actorContext: ActorContext, event: SujetoBajaFromObjetoSet): Unit = {

    val isExclusionSujeto = QueryExclusionSujeto(event.sujetoId)
    println("DIFERENCIA -> " + currentState.diffStates)
    if (currentState.diffStates) {
      println("TREINTA 11.2 " + currentState)
      sender ! ObjetoUpdateFromSujeto(
        currentState.lastDeliveryIdByEvents,
        event.sujetoId,
        """Objeto-(.*?)-""".r.findFirstMatchIn(sender.path.toString) match {
          case Some(matched) => matched.group(1)
          case None => ""
        },
        sender.path.toString.last.toString,
        currentState.deuda30Sujeto,
        if (isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head
      )
    } else {
      children.foreach(actor => {
        val actorSelection = actorContext.actorSelection(actor.path)
        actorSelection ! ObjetoUpdateFromSujeto(
          currentState.lastDeliveryIdByEvents,
          event.sujetoId,
          """Objeto-(.*?)-""".r.findFirstMatchIn(actor.path.toString) match {
            case Some(matched) => matched.group(1)
            case None => ""
          },
          actor.path.toString.last.toString,
          currentState.deuda30Sujeto,
          if (isExclusionSujeto.isEmpty) "" else isExclusionSujeto.head

        )
      })
    }
  }

}
