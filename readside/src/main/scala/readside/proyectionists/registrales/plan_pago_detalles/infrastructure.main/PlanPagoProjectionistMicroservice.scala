package readside.proyectionists.registrales.plan_pago_detalles.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import readside.proyectionists.registrales.plan_pago_detalles.{PlanPagoRemovedFromDtoHandler, PlanPagoUpdatedFromDtoHandler}

class PlanPagoProjectionistMicroservice(
    implicit m: KafkaConsumerMicroserviceRequirements
) extends KafkaConsumerMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new PlanPagoUpdatedFromDtoHandler,
      new PlanPagoRemovedFromDtoHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
