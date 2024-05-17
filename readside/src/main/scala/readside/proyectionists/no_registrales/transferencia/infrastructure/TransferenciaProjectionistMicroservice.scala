package readside.proyectionists.no_registrales.transferencia.infrastructure

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import readside.proyectionists.no_registrales.transferencia.TransferenciaSnapshotPersistedHandler

class TransferenciaProjectionistMicroservice(
                                                   implicit m: KafkaConsumerMicroserviceRequirements
                                                 ) extends KafkaConsumerMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new TransferenciaSnapshotPersistedHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}


