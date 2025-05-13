package readside.proyectionists.registrales.objeto_juicio

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.ObjetoJuicioUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.objeto_juicio.projections.ObjetoJuicioUpdatedFromDtoProjection

import scala.concurrent.Future

class ObjetoJuicioUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObjetoJuicioUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObjetoJuicioUpdatedFromDto"
  override def topicRetry: String = "ObjetoJuicioUpdatedFromDto_retry"
  override def topicError: String = "ObjetoJuicioUpdatedFromDto_error"

  import consumers.registral.objeto_juicio.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObjetoJuicioUpdatedFromDto] = {
    decode[ObjetoJuicioUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()

  override def processMessage(registro: ObjetoJuicioUpdatedFromDto): Future[Response.SuccessProcessing] = {
    val projection = ObjetoJuicioUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
