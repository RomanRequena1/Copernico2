package consumers_spec.no_registrales.obligacion.unit_test

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers_spec.no_registrales.obligacion.ObligacionSpec
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
import design_principles.external_pub_sub.kafka.KafkaMock

object ObligacionSpecUT {
  def getContext(system: ActorSystem): ObligacionSpec.TestContext = {

    val vinculoActor: ActorRef =
      ObjetoVinculoActor.startWithRequirements(MonitoringAndMessageProducerMock.dummy)(system)

    val ObligacionSpecMessageBroker = new KafkaMock()

    val ObligacionSpecQuery = {
      val sujetoActor =
        SujetoActor
          .startWithRequirements(
            MonitoringAndMessageProducerMock.dummy
          )(system)
      new MessageTestkitUtils(sujetoActor)
        .StartMessageProcessor(ObligacionSpecMessageBroker)
        .startProcessing()
      new NoRegistralesQueryWithActorRef(
        sujetoActor,
        vinculoActor
      )
    }
    ObligacionSpec TestContext (
      messageProducer = ObligacionSpecMessageBroker,
      messageProcessor = ObligacionSpecMessageBroker,
      Query = ObligacionSpecQuery
    )
  }
}

class ObligacionSpecUT
    extends ObligacionSpec(
      ObligacionSpecUT.getContext
    )
