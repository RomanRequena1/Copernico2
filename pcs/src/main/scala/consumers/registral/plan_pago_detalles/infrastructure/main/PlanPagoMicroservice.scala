package consumers.registral.plan_pago_detalles.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.registral.plan_pago_detalles.domain.PlanPagoState
import consumers.registral.plan_pago_detalles.infrastructure.dependency_injection.PlanPagoActor
import consumers.registral.plan_pago_detalles.infrastructure.http.PlanPagoStateAPI
import consumers.registral.plan_pago_detalles.infrastructure.kafka.{PlanPagoNoTributarioTransaction, PlanPagoTributarioTransaction}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class PlanPagoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {
  implicit val actor: PlanPagoActor = PlanPagoActor(PlanPagoState())(monitoringAndMessageProducer.messageProducer, system)
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      PlanPagoNoTributarioTransaction(actor, monitoring),
      PlanPagoTributarioTransaction(actor, monitoring)
    )

  override def route: Route =
    (Seq(
      PlanPagoStateAPI(actor, monitoring).route
    ) ++ actorTransactions.map(_.route)) reduce (_ ~ _)

}
