package readside.proyectionists.registrales.juicio_tri.infrastructure.main

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import readside.proyectionists.registrales.juicio_tri.{JuicioDosRemovedSnapshotHandler, JuicioDosUpdatedSnapshotHandler}
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}

class JuicioDosProjectionistMicroservice(
    implicit m: KafkaConsumerMicroserviceRequirements
    ) extends KafkaConsumerMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new JuicioDosUpdatedSnapshotHandler,
      new JuicioDosRemovedSnapshotHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)
}

