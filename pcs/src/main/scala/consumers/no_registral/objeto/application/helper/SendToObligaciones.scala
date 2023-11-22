package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorContext, ActorRef}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromSujeto
import consumers.no_registral.objeto.domain.ObjetoState
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.{ObligacionRemove, ObligacionRemoveInfoFromObjeto}
import consumers.no_registral.sujeto.domain.SujetoState

object SendToObligaciones {
  def apply(newState: ObjetoState, actorContext: ActorContext) : Unit = {

        actorContext.children.foreach( actor => {
          val actorSelection = actorContext.actorSelection(actor.path)
          actorSelection ! ObligacionRemoveInfoFromObjeto(
            newState.lastDeliveryIdByEvents,
            newState.registro.get.SOJ_SUJ_IDENTIFICADOR,
            newState.registro.get.SOJ_IDENTIFICADOR,
            newState.registro.get.SOJ_TIPO_OBJETO,
            """Obligacion-(\d+)""".r.findFirstMatchIn(actor.path.toString) match {
              case Some(matched) => matched.group(1)
              case None => ""
            }

          )
        })
      }
}
