package consumers.registral.cupon_descuento.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.cupon_descuento.domain.CuponDescuentoState
import consumers.registral.cupon_descuento.infrastructure.consumer.CuponDescuentoTributarioTransaction
import consumers.registral.cupon_descuento.infrastructure.dependency_injection.CuponDescuentoActor
import consumers.registral.cupon_descuento.infrastructure.http.CuponDescuentoStateAPI
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import org.slf4j.LoggerFactory

class CuponDescuentoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: CuponDescuentoActor = CuponDescuentoActor(CuponDescuentoState())
  private val log = LoggerFactory.getLogger(this.getClass)
  log.error("Cumbia 1"  )
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      CuponDescuentoTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      CuponDescuentoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)
}
