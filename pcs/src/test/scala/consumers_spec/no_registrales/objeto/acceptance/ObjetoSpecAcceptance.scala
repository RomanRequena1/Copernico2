//package consumers_spec.no_registrales.objeto.acceptance
//
//import akka.actor.{ActorRef, ActorSystem}
//import akka.entity.ShardedEntity.ShardedEntityRequirements
//import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
//import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
//import consumers_spec.no_registrales.objeto.ObjetoSpec
//import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
//import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
//import design_principles.external_pub_sub.kafka.{KafkaMock, KafkaProduction}
//import org.scalatest.Ignore
//
//object ObjetoSpecAcceptance {
//  def getContext(system: ActorSystem): ObjetoSpec.TestContext = {
//    val vinculoActor: ActorRef =
//      ObjetoVinculoActor.startWithRequirements(MonitoringAndMessageProducerMock.production(system))(system)
//    val ObjetoSpecMessageBroker = new KafkaProduction()(system) // TODO implement createTopic for the real KafkaProducer and use it
//    val ObjetoSpecQuery = {
//      val sujetoActor =
//        SujetoActor
//          .startWithRequirements(
//            MonitoringAndMessageProducerMock.production(system)
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
//class ObjetoSpecAcceptance
//    extends ObjetoSpec(
//      ObjetoSpecAcceptance.getContext
//    )
