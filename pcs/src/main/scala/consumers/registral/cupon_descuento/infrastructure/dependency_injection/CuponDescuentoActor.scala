package consumers.registral.cupon_descuento.infrastructure.dependency_injection

import akka.actor.typed.ActorSystem
import consumers.registral.cupon_descuento.application.cqrs.commands.CuponDescuentoUpdateFromDtoHandler
import consumers.registral.cupon_descuento.application.cqrs.queries.GetStateCuponDescuentoHandler
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoCommands.CuponDescuentoUpdateFromDto
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoMessage
import consumers.registral.cupon_descuento.application.entities.CuponDescuentoQueries.GetStateCuponDescuento
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.CuponDescuentoUpdatedFromDto
import consumers.registral.cupon_descuento.domain.events.CuponDescuentoUpdatedFromDtoHandler
import consumers.registral.cupon_descuento.domain.{CuponDescuentoEvents, CuponDescuentoState}
import cqrs.base_actor.typed.BasePersistentShardedTypedActorWithCQRS
import kafka.MessageProducer

case class CuponDescuentoActor(state: CuponDescuentoState =  CuponDescuentoState())(
  implicit
  messageProducer: MessageProducer,
  system: ActorSystem[Nothing]
) extends BasePersistentShardedTypedActorWithCQRS[
  CuponDescuentoMessage,
  CuponDescuentoEvents,
  CuponDescuentoState
](state) {

  commandBus.subscribe[CuponDescuentoUpdateFromDto](new CuponDescuentoUpdateFromDtoHandler().handle)
  queryBus.subscribe[GetStateCuponDescuento](new GetStateCuponDescuentoHandler().handle)
  eventBus.subscribe[CuponDescuentoUpdatedFromDto](new CuponDescuentoUpdatedFromDtoHandler().handle)

//  override def getTags(event: CuponDescuentoEvents): Set[String] = {
//    event match {
//      case _ => Set("CuponDescuento-updated")
//    }
//  }
}
