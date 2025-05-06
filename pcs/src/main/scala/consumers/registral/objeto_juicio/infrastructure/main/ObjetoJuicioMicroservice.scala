package consumers.registral.objeto_juicio.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.objeto_juicio.domain.ObjetoJuicioState
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import consumers.registral.objeto_juicio.infrastructure.http.ObjetoJuicioStateAPI
import consumers.registral.objeto_juicio.infrastructure.kafka.{ObjetoJuicioNoTributarioTransaction, ObjetoJuicioTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class ObjetoJuicioMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: ObjetoJuicioActor = ObjetoJuicioActor(ObjetoJuicioState())

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ObjetoJuicioNoTributarioTransaction(actor, monitoring),
      ObjetoJuicioTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      ObjetoJuicioStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)
}
