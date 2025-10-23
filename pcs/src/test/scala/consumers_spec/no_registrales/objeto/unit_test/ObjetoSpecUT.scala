//package consumers_spec.no_registrales.objeto.unit_test
//
//import akka.actor.{ActorRef, ActorSystem}
//import akka.entity.ShardedEntity.ShardedEntityRequirements
//import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
//import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
//import consumers_spec.no_registrales.objeto.ObjetoSpec
//import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
//import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
//import design_principles.external_pub_sub.kafka.KafkaMock
//
//object ObjetoSpecUT {
//  def getContext(system: ActorSystem): ObjetoSpec.TestContext = {
//    val vinculoActor: ActorRef =
//      ObjetoVinculoActor.startWithRequirements(MonitoringAndMessageProducerMock.dummy)(system)
//
//    val ObjetoSpecMessageBroker = new KafkaMock()
//
//    val ObjetoSpecQuery = {
//      val sujetoActor =
//        SujetoActor
//          .startWithRequirements(
//            MonitoringAndMessageProducerMock.dummy
//          )(system)
//      new MessageTestkitUtils(sujetoActor)
//        .StartMessageProcessor(ObjetoSpecMessageBroker)
//        .startProcessing()
//      new NoRegistralesQueryWithActorRef(
//        sujetoActor,
//        vinculoActor
//      )
//    }
//    ObjetoSpec TestContext (
//      messageProducer = ObjetoSpecMessageBroker,
//      messageProcessor = ObjetoSpecMessageBroker,
//      Query = ObjetoSpecQuery
//    )
//  }
//}
//
//class ObjetoSpecUT
//    extends ObjetoSpec(
//      ObjetoSpecUT.getContext
//    )
