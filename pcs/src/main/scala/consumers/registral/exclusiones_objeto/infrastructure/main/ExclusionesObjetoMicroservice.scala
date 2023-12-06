package consumers.registral.exclusiones_objeto.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.exclusiones_objeto.domain.ExclusionesObjetoState
import consumers.registral.exclusiones_objeto.infrastructure.dependency_injection.ExclusionesObjetoActor
import consumers.registral.exclusiones_objeto.infrastructure.http.ExclusionesObjetoStateAPI
import consumers.registral.exclusiones_objeto.infrastructure.kafka.ExclusionesObjetoTributarioTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class ExclusionesObjetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements)
  extends KafkaConsumerMicroservice {
  implicit val actor: ExclusionesObjetoActor = ExclusionesObjetoActor(ExclusionesObjetoState())
  println("CUMBIA 1-> " + actor)

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      //ExclusionesSujetoNoTributarioTransaction(actor, monitoring),
      ExclusionesObjetoTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      ExclusionesObjetoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}

