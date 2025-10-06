package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.{CassandraWrite, CassandraWriteProduction}
import design_principles.actor_model.mechanism.local_processing.LocalizedProcessingMessageExtractor
import kafka.{KafkaMessageProducer, MessageProducer}
import monitoring.{KamonMonitoring, Monitoring}

import scala.util.Try

trait ShardedEntity[Requirements] extends ClusterEntity[Requirements] {

  import ShardedEntity._

  def props(requirements: Requirements): Props

  val NR_PARTITIONS: Int = Try(System.getenv("NR_PARTITIONS")).map(_.toInt).getOrElse(90)

  val extractEntityId: ShardRegion.ExtractEntityId = {
    case s: Sharded => (s.entityId, s)
  }

  def extractShardId: ShardRegion.ExtractShardId = {
    case s: Sharded =>
      new LocalizedProcessingMessageExtractor(NR_PARTITIONS).shardId(s.shardedId)
  }

  def clusterShardingSettings(
      implicit
      system: ActorSystem
  ) = ClusterShardingSettings(system)

  def startWithRequirements(requirements: Requirements)(
      implicit
      system: ActorSystem
  ): ActorRef = ClusterSharding(system).start(
    typeName = typeName,
    entityProps = props(requirements), //TODO .withDispatcher("my-dispatcher"),
    settings = clusterShardingSettings,
    extractEntityId = extractEntityId,
    extractShardId = extractShardId
  )
}

object ShardedEntity {

  trait MonitoringAndCassandraWrite {
    val monitoring: Monitoring
    val cassandraWrite: CassandraWrite
    val actorTransactionRequirements: ActorTransactionRequirements

    def ec = actorTransactionRequirements.executionContext
  }

  case class ProductionMonitoringAndCassandraWrite(
      monitoring: KamonMonitoring,
      cassandraWrite: CassandraWriteProduction,
      actorTransactionRequirements: ActorTransactionRequirements
  ) extends MonitoringAndCassandraWrite

  trait MonitoringAndMessageProducer {
    val monitoring: Monitoring
    val messageProducer: MessageProducer
    val psrmMessageProducer: MessageProducer
    println("estoy en el MonitoringAndMessageProducer")
  }

  trait MonitoringAndMessageProducerTranf {
    val monitoring: Monitoring
  }
  case class ProductionMonitoringAndMessageProducer(
      monitoring: KamonMonitoring,
      messageProducer: KafkaMessageProducer,
      psrmMessageProducer: KafkaMessageProducer
  ) extends MonitoringAndMessageProducer

  case class ProductionMonitoringAndMessageProducerTransf(
      monitoring: KamonMonitoring
  ) extends MonitoringAndMessageProducerTranf
  case class ShardedEntityRequirements(
      system: ActorSystem
  )

  trait ShardedEntityNoRequirements extends ShardedEntity[ShardedEntity.NoRequirements] {

    def start(
        implicit
        system: ActorSystem
    ): ActorRef = this.startWithRequirements(NoRequirements())
  }

  case class NoRequirements()

  trait Sharded {
    def entityId: String
    def shardedId: String
    def tupled: (String, String) = (entityId, shardedId)
  }
}
