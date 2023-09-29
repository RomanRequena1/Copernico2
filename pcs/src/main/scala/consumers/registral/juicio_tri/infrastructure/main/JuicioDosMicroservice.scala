package consumers.registral.juicio_tri.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.juicio_tri.domain.JuicioDosState
import consumers.registral.juicio_tri.infrastructure.dependency_injection.JuicioDosActor
import consumers.registral.juicio_tri.infrastructure.http.JuicioDosStateAPI
import consumers.registral.juicio_tri.infrastructure.kafka.JuicioDosTributarioTransaction
import design_principles.microservice.kafka_consumer_microservice.{
  KafkaConsumerMicroservice,
  KafkaConsumerMicroserviceRequirements
}
class JuicioDosMicroservice (implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: JuicioDosActor = JuicioDosActor(JuicioDosState())

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      JuicioDosTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      //JuicioDosStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)
}
