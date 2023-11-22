package consumers.registral.exclusiones_sujeto.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoState
import consumers.registral.exclusiones_sujeto.infrastructure.dependency_injection.ExclusionesSujetoActor
import consumers.registral.exclusiones_sujeto.infrastructure.http.ExclusionesSujetoStateAPI
import consumers.registral.exclusiones_sujeto.infrastructure.kafka.ExclusionesSujetoTributarioTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class ExclusionesSujetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements)
  extends KafkaConsumerMicroservice {
  implicit val actor: ExclusionesSujetoActor = ExclusionesSujetoActor(ExclusionesSujetoState())
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      //ExclusionesSujetoNoTributarioTransaction(actor, monitoring),
      ExclusionesSujetoTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      ExclusionesSujetoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}

