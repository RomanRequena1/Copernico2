package readside.proyectionists.registrales.componente_i.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import readside.proyectionists.registrales.componente_i.ComponenteIPersistedSnapshotHandler

class ComponenteIProjectionistMicroservice(
    implicit m: KafkaConsumerMicroserviceRequirements
) extends KafkaConsumerMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new ComponenteIPersistedSnapshotHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
