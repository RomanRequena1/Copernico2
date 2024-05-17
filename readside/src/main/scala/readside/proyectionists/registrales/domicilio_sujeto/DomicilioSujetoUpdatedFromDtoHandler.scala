package readside.proyectionists.registrales.domicilio_sujeto
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.domicilio_sujeto.domain.DomicilioSujetoEvents.DomicilioSujetoUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.domicilio_sujeto.projections.DomicilioSujetoUpdatedFromDtoProjection

import scala.concurrent.Future
class DomicilioSujetoUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[DomicilioSujetoUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "DomicilioSujetoUpdatedFromDto"
  override def topicRetry: String = "DomicilioSujetoUpdatedFromDto_retry"
  override def topicError: String = "DomicilioSujetoUpdatedFromDto_error"

  import consumers.registral.domicilio_sujeto.infrastructure.json._

  override def processInput(input: String): Either[Throwable, DomicilioSujetoUpdatedFromDto] =
    decode[DomicilioSujetoUpdatedFromDto](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: DomicilioSujetoUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = DomicilioSujetoUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
