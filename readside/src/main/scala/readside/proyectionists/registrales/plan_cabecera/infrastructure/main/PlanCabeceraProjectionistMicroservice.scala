package readside.proyectionists.registrales.plan_cabecera.infrastructure.main
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import readside.proyectionists.registrales.plan_cabecera.{PlanCabeceraRemovedFromDtoHandler, PlanCabeceraUpdatedFromDtoHandler}
class PlanCabeceraProjectionistMicroservice (
                                              implicit m: KafkaConsumerMicroserviceRequirements
                                            ) extends KafkaConsumerMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new PlanCabeceraUpdatedFromDtoHandler,
      new PlanCabeceraRemovedFromDtoHandler
    )
  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
