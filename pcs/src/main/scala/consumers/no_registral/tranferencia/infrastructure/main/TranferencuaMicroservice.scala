package consumers.no_registral.tranferencia.infrastructure.main
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers.no_registral.tranferencia.infrastructure.http.ObjetoVinculoStateAPI
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
class TranferencuaMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice  {
  implicit val objV = ObjetoVinculoActor.startWithRequirements(monitoringAndMessageProducer)




  override def actorTransactions: Set[ActorTransaction[_]] = Set()


  override def route: Route = {
    (Seq(
      ObjetoVinculoStateAPI(objV, monitoring).route
    ) ++
      actorTransactions.map(_.route).toSeq) reduce (_ ~ _)
  }

}

