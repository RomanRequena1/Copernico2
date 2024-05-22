package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.{CassandraWrite, CassandraWriteProduction}
import design_principles.actor_model.{Command, Query}
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

  val numberOfShards = 9
  def extractShardId: ShardRegion.ExtractShardId = {
    case s: Command => {
      s.aggregateRoot match {
        case s"Sujeto-$sujetoId-Objeto-$objetoId-$tipoObjeto-Obligacion-$obligacionId" => {
          val idParaShardear: String = sujetoId + "-" + objetoId
          new LocalizedProcessingMessageExtractor(NR_PARTITIONS).shardId(idParaShardear)
        }
        case _ => new LocalizedProcessingMessageExtractor(NR_PARTITIONS).shardId(s.shardedId)
      }
    }
    case q: Query => {
      new LocalizedProcessingMessageExtractor(NR_PARTITIONS).shardId(q.shardedId)
    }

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
  }
  trait MonitoringAndMessageProducerTranf {
    val monitoring: Monitoring
  }
  case class ProductionMonitoringAndMessageProducer(
                                                     monitoring: KamonMonitoring,
                                                     messageProducer: KafkaMessageProducer
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
