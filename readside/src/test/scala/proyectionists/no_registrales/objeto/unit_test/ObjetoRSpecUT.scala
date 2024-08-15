package proyectionists.no_registrales.objeto.unit_test

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import proyectionists.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
import design_principles.external_pub_sub.kafka.{KafkaMock, KafkaProduction}
import design_principles.projection.mock.CassandraWriteMock
import proyectionists.no_registrales.objeto.ObjetoRSpec

object ObjetoRSpecUT {
  def getContext(system: ActorSystem): ObjetoRSpec.TestContext = {
    val ObjetoSpecMessageBroker = new KafkaMock()

    new MessageTestkitUtils(system)
      .StartMessageProcessor(ObjetoSpecMessageBroker)
      .startProcessing()


    ObjetoRSpec.TestContext (
      messageProducer = ObjetoSpecMessageBroker,
      messageProcessor = ObjetoSpecMessageBroker,
      cassandra = MockMonitoringAndCassandraWrite.apply(new CassandraWriteMock())
    )
  }
}

class ObjetoRSpecUT
    extends ObjetoRSpec(
      ObjetoRSpecUT.getContext
    )
