package consumers.no_registral.objeto.application.helper

import consumers.no_registral.objeto.application.entities.ObjetoCommands.RemoveObjetoFromObligacion
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.obligacion.application.entities.ObligacionCommands.ObligacionRemoveInfoFromObjeto

object SendToObligaciones {
  def apply(objActor: ObjetoActor) : Unit = {

    objActor.state.obnVencidas match {
      case x if x.isEmpty => {
        objActor.context.self ! RemoveObjetoFromObligacion(
          objActor.state.lastDeliveryIdByEvents,
          objActor.state.registro.get.SOJ_SUJ_IDENTIFICADOR,
          objActor.state.registro.get.SOJ_IDENTIFICADOR,
          objActor.state.registro.get.SOJ_TIPO_OBJETO,
          "0",
          Some("0")
        )
      }

      case x => x.foreach( obn => {
        val actorSelection = objActor.context.parent
        actorSelection ! ObligacionRemoveInfoFromObjeto(
          objActor.state.lastDeliveryIdByEvents,
          objActor.state.registro.get.SOJ_SUJ_IDENTIFICADOR,
          objActor.state.registro.get.SOJ_IDENTIFICADOR,
          objActor.state.registro.get.SOJ_TIPO_OBJETO,
          obn._1
        )
      })
    }
  }
}
