package consumers.registral.juicio_obn.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.juicio_obn.domain.JuicioObnState
import consumers.registral.juicio_obn.infrastructure.dependency_injection.JuicioObnActor
import consumers.registral.juicio_obn.infrastructure.http.JuicioObnStateAPI
import consumers.registral.juicio_obn.infrastructure.kafka.JuicioObnTributarioTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class JuicioObnMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice{
  implicit val actor: JuicioObnActor = JuicioObnActor(JuicioObnState())
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      JuicioObnTributarioTransaction(actor, monitoring)
    )


  override def route: Route =
    (Seq(
    JuicioObnStateAPI(actor, monitoring).route
  ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)
}
