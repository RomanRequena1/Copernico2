package readside.proyectionists.no_registrales.exclusiones_objeto.infrastructure.main

import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import api.actor_transaction.ActorTransaction
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import readside.proyectionists.no_registrales.exclusiones_objeto.ExclusionesObjetoUpdatedFromDtoHandler


class ExclusionesObjetoProjectionistMicroservice(
                                                  implicit m: KafkaConsumerMicroserviceRequirements
                                                ) extends KafkaConsumerMicroservice {

  override def actorTransactions: Set[ActorTransaction[_]] =
    Set(
      new ExclusionesObjetoUpdatedFromDtoHandler
    )

  override def route: Route =
    actorTransactions.map(_.route) reduce (_ ~ _)

}
