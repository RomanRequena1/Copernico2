package consumers.no_registral.exclusiones_objeto.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.exclusiones_objeto.infrastructure.dependency_injection.ExclusionesObjetoActor
import consumers.no_registral.exclusiones_objeto.infrastructure.http.ExclusionesObjetoStateAPI
import consumers.no_registral.exclusiones_objeto.infrastructure.kafka.ExclusionesObjetoTributarioTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class ExclusionesObjetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: ActorRef =
    ExclusionesObjetoActor.startWithRequirements(monitoringAndMessageProducer)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ExclusionesObjetoTributarioTransaction(actor, monitoring)
    )
  def route: Route =
    (
      Set(
        ExclusionesObjetoStateAPI(actor, monitoring).route
      ) ++ actorTransactions.map(_.route)
      ).reduce(_ ~ _)
}
