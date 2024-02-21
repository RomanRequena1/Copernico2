package consumers.no_registral.tranferencia.infrastructure.main
import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers.no_registral.tranferencia.infrastructure.http.ObjetoVinculoStateAPI
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}


