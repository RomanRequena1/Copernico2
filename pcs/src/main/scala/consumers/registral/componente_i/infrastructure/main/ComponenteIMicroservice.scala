package consumers.registral.componente_i.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.componente_i.domain.ComponenteIState
import consumers.registral.componente_i.infrastructure.consumer.ComponenteITributarioTransaction
import consumers.registral.componente_i.infrastructure.dependency_injection.ComponenteIActor
import consumers.registral.componente_i.infrastructure.http.ComponenteIStateAPI
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import org.slf4j.LoggerFactory

class ComponenteIMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: ComponenteIActor = ComponenteIActor(ComponenteIState())
  private val log = LoggerFactory.getLogger(this.getClass)
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ComponenteITributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      ComponenteIStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)
}
