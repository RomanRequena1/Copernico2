//package proyectionists.no_registrales.base_Tri.acceptance
//
//import akka.actor.ActorSystem
//import cassandra.MockMonitoringAndCassandraWrite
//import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
//import design_principles.external_pub_sub.kafka.KafkaProduction
//import design_principles.projection.mock.CassandraWriteMock
//import proyectionists.no_registrales.base_Tri.BaseTriSpec
//import proyectionists.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
//
//object BaseTri {
//  def getContext(system: ActorSystem): BaseTriSpec.TestContext = {
//    val ObjetoSpecMessageBroker = new KafkaProduction()(system)
//    val sujetoActor = SujetoActor.startWithRequirements(
//      MonitoringAndMessageProducerMock.production(system, ObjetoSpecMessageBroker)
//    )(system)
//
//    new MessageTestkitUtils(system, sujetoActor)
//      .StartMessageProcessor(ObjetoSpecMessageBroker)
//      .startProcessing()
//
//    BaseTriSpec.TestContext(
//      messageProducer = ObjetoSpecMessageBroker,
//      messageProcessor = ObjetoSpecMessageBroker,
//      cassandra = MockMonitoringAndCassandraWrite.apply(new CassandraWriteMock())
//    )
//  }
//}
//
//class BaseTri
//    extends BaseTriSpec(
//      BaseTri.getContext
//    )
