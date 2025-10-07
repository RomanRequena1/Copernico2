package design_principles.microservice.kafka_consumer_microservice

import api.actor_transaction.{ActorTransaction, ActorTransactionController}
import design_principles.actor_model.mechanism.QueryStateAPI.QueryStateApiRequirements
import design_principles.microservice.Microservice
import kafka.{KafkaMessageProcessorRequirements, KafkaMessageProducer}
import monitoring.KamonMonitoring
import akka.actor.typed.scaladsl.adapter._
import akka.entity.ShardedEntity.{ProductionMonitoringAndCassandraWrite, ProductionMonitoringAndMessageProducer, ProductionMonitoringAndMessageProducerTransf}
import design_principles.actor_model.mechanism.stream_supervision.UniqueTopicPerNode.uniqueTopicPerNode

import scala.util.{Failure, Success}

abstract class KafkaConsumerMicroservice(implicit m: KafkaConsumerMicroserviceRequirements)
  extends Microservice[KafkaConsumerMicroserviceRequirements] {

  implicit final val classicSystem: akka.actor.ActorSystem = m.ctx
  implicit final val system: akka.actor.typed.ActorSystem[Nothing] = m.ctx.toTyped

  implicit final val monitoring: KamonMonitoring = m.monitoring
  implicit final val queryStateApiR: QueryStateApiRequirements = m.queryStateApiRequirements
  implicit final val kafkaMessageProcessorR: KafkaMessageProcessorRequirements = m.kafkaMessageProcessorRequirements
  implicit final val actorTransactionR: ActorTransaction.ActorTransactionRequirements = m.actorTransactionRequirements

  println(s"========================================")
  println(s"[MICROSERVICE] 🚀 Iniciando KafkaConsumerMicroservice")
  println(s"========================================")

  implicit val messageProducer: KafkaMessageProducer =
    KafkaMessageProducer(monitoring, m.kafkaMessageProcessorRequirements.rebalancerListener)

  println(s"[MICROSERVICE] ✅ MessageProducer principal creado")

  // PSRM Producer con fallback seguro
  implicit val psrmMessageProducer: KafkaMessageProducer = {
    KafkaMessageProducer.psrmProducer(monitoring, m.kafkaMessageProcessorRequirements.rebalancerListener) match {
      case Success(producer) =>
        println(s"[MICROSERVICE] ✅ PSRM MessageProducer creado exitosamente")
        producer

      case Failure(exception) =>
        println(s"[MICROSERVICE] ⚠️ No se pudo crear PSRM producer: ${exception.getMessage}")
        println(s"[MICROSERVICE] ⚠️ FALLBACK: Usando producer principal para PSRM")
        messageProducer // Usa el producer principal como fallback
    }
  }

  implicit final val monitoringAndMessageProducer: ProductionMonitoringAndMessageProducer =
    ProductionMonitoringAndMessageProducer(
      monitoring,
      messageProducer,
      psrmMessageProducer
    )

  println(s"[MICROSERVICE] ✅ ProductionMonitoringAndMessageProducer configurado")
  println(s"========================================")

  implicit final val monitoringAndMessageProducerTransf: ProductionMonitoringAndMessageProducerTransf =
    ProductionMonitoringAndMessageProducerTransf(
      monitoring
    )
  implicit final val monitoringAndCassandraWrite: ProductionMonitoringAndCassandraWrite =
    ProductionMonitoringAndCassandraWrite(
      monitoring,
      m.cassandraWrite,
      actorTransactionR
    )

  def actorTransactions: Set[ActorTransaction[_]]

  final def actorTransactionControllers: Set[(String, ActorTransactionController)] =
    actorTransactions.map { actorTransaction =>
      (uniqueTopicPerNode(actorTransaction.topic), actorTransaction.controller)
    }
}
