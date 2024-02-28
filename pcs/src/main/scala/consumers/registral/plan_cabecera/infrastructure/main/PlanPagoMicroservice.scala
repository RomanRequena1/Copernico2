package consumers.registral.plan_cabecera.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.plan_cabecera.domain.PlanCabeceraState
import consumers.registral.plan_cabecera.infrastructure.dependency_injection.PlanCabeceraActor
import consumers.registral.plan_cabecera.infrastructure.http.PlanCabeceraStateAPI
import consumers.registral.plan_cabecera.infrastructure.kafka.{PlanCabeceraNoTributarioTransaction, PlanCabeceraTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class PlanPagoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: PlanCabeceraActor = PlanCabeceraActor(PlanCabeceraState())
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      PlanCabeceraNoTributarioTransaction(actor, monitoring),
      PlanCabeceraTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      PlanCabeceraStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
