package readside.proyectionists.registrales.tramite
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.tramite.domain.TramiteEvents.TramiteUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.tramite.projections.TramiteUpdatedFromDtoProjection

import scala.concurrent.Future
class TramiteUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[TramiteUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "TramiteUpdatedFromDto"
  override def topicError: String = "TramiteUpdatedFromDto_error"
  override def topicRetry: String = "TramiteUpdatedFromDto_retry"

  import consumers.registral.tramite.infrastructure.json._

  override def processInput(input: String): Either[Throwable, TramiteUpdatedFromDto] =
    decode[TramiteUpdatedFromDto](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: TramiteUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = TramiteUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
