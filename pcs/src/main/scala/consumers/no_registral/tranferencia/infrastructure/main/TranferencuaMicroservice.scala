package consumers.no_registral.tranferencia.infrastructure.main
import akka.http.scaladsl.server.Directives._
import akka.actor.{ActorRef, Props}
import akka.http.scaladsl.model.Uri.Query.Empty
import akka.http.scaladsl.server.Route
import api.actor_transaction.ActorTransaction
import consumers.no_registral.tranferencia.application.entity.TranferenciaMessage.TranferenciaMessageRoots
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.TranferenciaActor
import consumers.no_registral.tranferencia.infrastructure.http.TransferenciaStateAPI
import design_principles.microservice.kafka_consumer_microservice.{KafkaConsumerMicroservice, KafkaConsumerMicroserviceRequirements}
import monitoring.KamonMonitoring


class TranferencuaMicroservice(implicit m: KafkaConsumerMicroserviceRequirements) extends KafkaConsumerMicroservice  {
    implicit val actorProp: Props = TranferenciaActor.props(monitoringAndMessageProducer)
    implicit val actorTranf: ActorRef = classicSystem.actorOf(actorProp)




    override def actorTransactions: Set[ActorTransaction[_]] = Set()


    override def route: Route = {
      (Seq(
        TransferenciaStateAPI(actorTranf, monitoring).route
      ) ++
        actorTransactions.map(_.route).toSeq) reduce (_ ~ _)
    }

  }