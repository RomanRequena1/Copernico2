package consumers.no_registral.objeto.infrastructure.main

import akka.actor.ActorRef
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.objeto.infrastructure.consumer.{ObjetoExencionTransaction, ObjetoNoTributarioTransaction, ObjetoTributarioTransaction, ObjetoUpdateNovedadTransaction}
import consumers.no_registral.objeto.infrastructure.http._
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
class ObjetoMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice {

  implicit val actor: ActorRef =
    SujetoActor.startWithRequirements(monitoringAndMessageProducer)
  ObjetoVinculoActor.startWithRequirements(monitoringAndMessageProducer)
  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      ObjetoExencionTransaction(actor, monitoring),
      ObjetoNoTributarioTransaction(actor, monitoring),
      ObjetoTributarioTransaction(actor, monitoring),
      ObjetoUpdateNovedadTransaction(actor, monitoring)
    )
  def route: Route =
    (
      Set(
        ObjetoStateAPI(actor, monitoring).route
      ) ++ actorTransactions.map(_.route)
    ).reduce(_ ~ _)

}
