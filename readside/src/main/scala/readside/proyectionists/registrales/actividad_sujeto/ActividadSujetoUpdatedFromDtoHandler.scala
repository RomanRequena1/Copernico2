package readside.proyectionists.registrales.actividad_sujeto
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.actividad_sujeto.domain.ActividadSujetoEvents.ActividadSujetoUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.actividad_sujeto.projections.ActividadSujetoUpdatedFromDtoProjection

import scala.concurrent.Future
class ActividadSujetoUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ActividadSujetoUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ActividadSujetoUpdatedFromDto"
  override def topicRetry: String = "ActividadSujetoUpdatedFromDto_retry"
  override def topicError: String = "ActividadSujetoUpdatedFromDto_error"

  import consumers.registral.actividad_sujeto.infrastructure.json.json._

  override def processInput(input: String): Either[Throwable, ActividadSujetoUpdatedFromDto] =
    decode[ActividadSujetoUpdatedFromDto](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ActividadSujetoUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = ActividadSujetoUpdatedFromDtoProjection(registro)
    projection.updateReadside()
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
