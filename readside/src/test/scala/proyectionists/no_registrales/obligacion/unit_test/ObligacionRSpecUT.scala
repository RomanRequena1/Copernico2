package proyectionists.no_registrales.obligacion.unit_test

import akka.actor.ActorSystem
import cassandra.MockMonitoringAndCassandraWrite
import design_principles.external_pub_sub.kafka.KafkaMock
import design_principles.projection.mock.CassandraWriteMock
import proyectionists.no_registrales.obligacion
import proyectionists.no_registrales.obligacion.ObligacionRSpec
import proyectionists.no_registrales.testkit.MessageTestkitUtils

object ObligacionRSpecUT {
  def getContext(system: ActorSystem): ObligacionRSpec.TestContext = {
    val ObligacionRSpecMessageBroker = new KafkaMock()

    new MessageTestkitUtils(system)
      .StartMessageProcessor(ObligacionRSpecMessageBroker)
      .startProcessing()

    obligacion.ObligacionRSpec.TestContext(
      messageProducer = ObligacionRSpecMessageBroker,
      messageProcessor = ObligacionRSpecMessageBroker,
      cassandra = MockMonitoringAndCassandraWrite.apply(new CassandraWriteMock())
    )
  }
}

class ObligacionRSpecUT
    extends ObligacionRSpec(
      ObligacionRSpecUT.getContext
    )
