package consumers.registral.juicio_obn.application.cqrs.queries

import consumers.registral.juicio_obn.application.entities.JuicioObnQueries.GetStateJuicioObn
import consumers.registral.juicio_obn.application.entities.JuicioObnResponses.GetJuicioObnResponses
import consumers.registral.juicio_obn.application.entities.{JuicioObnQueries, JuicioObnResponses}
import consumers.registral.juicio_obn.domain.{JuicioObnEvents, JuicioObnState}
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import cqrs.untyped.query.QueryHandler.SyncQueryHandler

import java.time.ZonedDateTime
import scala.util.{Success, Try}
import akka.actor.typed.ActorRef
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import kafka.MessageProducer

class GetStateJuicioObnHandler(actor: JuicioObnActor)(implicit messageProducer: MessageProducer){

  def handle(
              query: GetStateJuicioObn
            )(state: JuicioObnState)(replyTo: ActorRef[GetJuicioObnResponses]): ReplyEffect[JuicioObnEvents, JuicioObnState] = {
    println("CUMBIA -> " + state)
    Effect.reply[
      GetJuicioObnResponses,
      JuicioObnEvents,
      JuicioObnState
    ](replyTo)(
      GetJuicioObnResponses(state.registro, state.lastDeliveryIdByEvent, state.fechaUltMod)
    )
  }
}



