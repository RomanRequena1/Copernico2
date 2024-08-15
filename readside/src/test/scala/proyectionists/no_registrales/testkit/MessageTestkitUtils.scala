package proyectionists.no_registrales.testkit

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}
import akka.Done
import akka.actor.{ActorRef, ActorSystem, Props}
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.MockMonitoringAndCassandraWrite
import cassandra.write.CassandraWriteProduction
import com.typesafe.config.ConfigFactory
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.consumer._
import consumers.no_registral.obligacion.application.entities.{ObligacionExternalDto, ObligacionesAnt, ObligacionesTri}
import consumers.no_registral.obligacion.infrastructure.consumer.ObligacionTributariaTransaction
import consumers.no_registral.sujeto.application.entity.SujetoExternalDto
import consumers.no_registral.sujeto.infrastructure.consumer.SujetoTributarioTransaction
import design_principles.actor_model.Response
import design_principles.external_pub_sub.kafka.KafkaMock.MessageProcessorImplicits
import kafka.KafkaMessageProducer.KafkaKeyValue
import kafka.{KafkaMessageProcessorRequirements, MessageProcessor, MessageProducer, TopicListener}
import monitoring.{DummyMonitoring, KamonMonitoring}
import io.circe.syntax.EncoderOps
import consumers.no_registral.sujeto.infrastructure.json.SujetosImplicits.{
  SujetoExternalDtoEncoder,
  SujetoSnapshotPersistedEncoder
}
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.ObjetosTriEncoder
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits.ObjetoSnapshotPersistedEncoder
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits.{
  ObligacionExternalDtoEncoder,
  ObligacionPersistedSnapshotEncoder,
  ObligacionesAntDecoder,
  ObligacionesAntEncoder,
  ObligacionesTriEncoder
}
import consumers.no_registral.sujeto.domain.SujetoEvents.SujetoSnapshotPersisted
import design_principles.projection.mock.{CassandraTestkitMock, CassandraWriteMock}
import readside.proyectionists.no_registrales.objeto.ObjetoSnapshotPersistedHandler
import readside.proyectionists.no_registrales.obligacion.ObligacionPersistedSnapshotHandler
import readside.proyectionists.no_registrales.sujeto.SujetoSnapshotPersistedHandler

class MessageTestkitUtils(s: ActorSystem) {
//  implicit val actorTransactionRequirements: ActorTransactionRequirements = ActorTransactionRequirements(
//    executionContext = scala.concurrent.ExecutionContext.Implicits.global,
//    config = ConfigFactory.empty
//  )
//  val monitoring = new KamonMonitoring
//  val executionContext = scala.concurrent.ExecutionContext.Implicits.global
//  val rebalancerListener: ActorRef =
//    s.actorOf(
//      Props(
//        new TopicListener(
//          typeKeyName = "rebalancerListener",
//          monitoring
//        )
//      )
//    )
//
//  implicit val kafkaConsumerMicroserviceRequirements = KafkaConsumerMicroserviceRequirements(
//    monitoring = monitoring,
//    ctx = s,
//    queryStateApiRequirements = QueryStateApiRequirements(s, scala.concurrent.ExecutionContext.Implicits.global),
//    actorTransactionRequirements = actorTransactionRequirements,
//    kafkaMessageProcessorRequirements = KafkaMessageProcessorRequirements.productionSettings(rebalancerListener, monitoring, s, executionContext),
//    config = ConfigFactory.empty(),
//    cassandraWrite = new CassandraWriteProduction(),
//  )
  implicit val mockMonitoringAndCassandraWrite = MockMonitoringAndCassandraWrite.apply(new CassandraWriteMock())

  implicit class StartMessageProcessor(messageBroker: MessageProcessor with MessageProducer) {
    val monitoring = new DummyMonitoring
    def startProcessing(topics: Set[ActorTransaction[_]] = Set.empty): Unit = {

      (if (topics.isEmpty)
         Set(
           new ObjetoSnapshotPersistedHandler,
           new SujetoSnapshotPersistedHandler,
           new ObligacionPersistedSnapshotHandler
         ) // if no filter is set, then allow passthrough
       else topics)
        .foreach { transaction =>
          messageBroker.createTopic(transaction.topic)
          messageBroker.subscribeActorTransaction(
            transaction.topic,
            transaction
          )
        }

    }
  }

}

object MessageTestkitUtils {
  implicit class MessageProducerNoRegistrales(messageProducer: MessageProducer) {
    import consumers_spec.no_registrales.testsuite.ToJson._

    def produceObjetoReadside(objeto: ObjetoSnapshotPersisted): Future[akka.Done] = {
      def topic = "ObjetoSnapshotPersistedReadside"

//      println("Pr Obj Reads : " + objeto.asJson)
      messageProducer.produce(
        Seq(
          KafkaKeyValue(
            aggregateRoot = s"Sujeto-${objeto.sujetoId}-Objeto-${objeto.objetoId}-Tipo-${objeto.tipoObjeto}",
            json = objeto.asJson.toString()
          )
        ),
        topic
      )(_ => ())
    }

    def produceObligacionReadside(obligacion: ObligacionPersistedSnapshot): Future[akka.Done] = {
      def topic = "ObligacionPersistedSnapshot"

      println("Pr Obj Reads : " + obligacion.asJson)
      messageProducer.produce(
        Seq(
          KafkaKeyValue(
            aggregateRoot =
              s"Sujeto-${obligacion.sujetoId}-Objeto-${obligacion.objetoId}-Tipo-${obligacion.tipoObjeto}-Obligacion-${obligacion.obligacionId}",
            json = obligacion.asJson.toString()
          )
        ),
        topic
      )(_ => ())
    }

    def produceSujetoReadside(sujeto: SujetoSnapshotPersisted): Future[akka.Done] = {
      def topic = "SujetoSnapshotPersisted"

      messageProducer.produce(Seq(
                                KafkaKeyValue(
                                  aggregateRoot = s"Sujeto-${sujeto.sujetoId}",
                                  json = sujeto.asJson.toString()
                                )
                              ),
                              topic)(_ => ())
    }
  }
}
