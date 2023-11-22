package readside.proyectionists.registrales.exclusiones_sujeto

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import api.actor_transaction.ActorTransaction.ActorTransactionRequirements
import cassandra.write.CassandraWriteProduction
import consumers.registral.etapas_procesales.domain.EtapasProcesalesEvents.EtapasProcesalesUpdatedFromDto
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import monitoring.Monitoring
import readside.proyectionists.registrales.etapas_procesales.projections.EtapasProcesalesUpdatedFromDtoProjection
import io.circe.parser._
import readside.proyectionists.registrales.exclusiones_sujeto.projections.ExclusionesSujetoUpdatedFromDtoProjection
class ExclusionesSujetoUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ExclusionesSujetoUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ExclusionesSujetoUpdatedFromDto"
  override def topicRetry: String = "ExclusionesSujetoUpdatedFromDto_retry"
  override def topicError: String = "ExclusionesSujetoUpdatedFromDto_error"

  import consumers.registral.exclusiones_sujeto.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ExclusionesSujetoUpdatedFromDto] = {
    decode[ExclusionesSujetoUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ExclusionesSujetoUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = ExclusionesSujetoUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
