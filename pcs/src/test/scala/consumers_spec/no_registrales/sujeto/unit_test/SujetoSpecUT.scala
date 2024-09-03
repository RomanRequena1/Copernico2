package consumers_spec.no_registrales.sujeto.unit_test

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.ShardedEntityRequirements
import consumers.no_registral.sujeto.infrastructure.dependency_injection.SujetoActor
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import consumers_spec.no_registrales.sujeto.SujetoSpec
import consumers_spec.no_registrales.testkit.query.NoRegistralesQueryWithActorRef
import consumers_spec.no_registrales.testkit.{MessageTestkitUtils, MonitoringAndMessageProducerMock}
import design_principles.external_pub_sub.kafka.KafkaMock

object SujetoSpecUT {
  def getContext(system: ActorSystem): SujetoSpec.TestContext = {

    val vinculoActor: ActorRef =
      ObjetoVinculoActor.startWithRequirements(MonitoringAndMessageProducerMock.dummy)(system)

    val SujetoSpecMessageBroker = new KafkaMock()

    val SujetoSpecQueryAgainstActors = {
      val sujetoActor =
        SujetoActor
          .startWithRequirements(
            MonitoringAndMessageProducerMock.dummy.copy(
              messageProducer = SujetoSpecMessageBroker
            )
          )(system)
      new MessageTestkitUtils(sujetoActor)
        .StartMessageProcessor(SujetoSpecMessageBroker)
        .startProcessing()
      new NoRegistralesQueryWithActorRef(
        sujetoActor,
        vinculoActor
      )
    }
    SujetoSpec TestContext (
      messageProducer = SujetoSpecMessageBroker,
      messageProcessor = SujetoSpecMessageBroker,
      Query = SujetoSpecQueryAgainstActors
    )
  }
}

class SujetoSpecUT
    extends SujetoSpec(
      SujetoSpecUT getContext
    )
