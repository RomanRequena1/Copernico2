package proyectionists.no_registrales.objeto.acceptance

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import proyectionists.no_registrales.testkit.MonitoringAndMessageProducerMock
import design_principles.external_pub_sub.kafka.{KafkaMock, KafkaProduction}
import design_principles.microservice.kafka_consumer_microservice.ProductionMicroserviceContextProvider
import design_principles.projection.mock.CassandraWriteMock
import kafka.KafkaMessageProcessorRequirements
import proyectionists.no_registrales.objeto.ObjetoRSpec
import proyectionists.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}

object ObjetoRSpecAcceptance {
  def getContext(system: ActorSystem): ObjetoRSpec.TestContext = {
    val ObjetoSpecMessageBroker = new KafkaProduction()(system)
    val sujetoActor = SujetoActor.startWithRequirements(MonitoringAndMessageProducerMock.production(system, ObjetoSpecMessageBroker))(system)

    new MessageTestkitUtils(system, sujetoActor)
      .StartMessageProcessor(ObjetoSpecMessageBroker)
      .startProcessing()

    ObjetoRSpec.TestContext (
      messageProducer = ObjetoSpecMessageBroker,
      messageProcessor = ObjetoSpecMessageBroker,
      cassandra = MockMonitoringAndCassandraWrite.apply(new CassandraWriteMock())
    )
  }
}

class ObjetoRSpecAcceptance
    extends ObjetoRSpec(
      ObjetoRSpecAcceptance.getContext
    )
