package readside.proyectionists.registrales.parametrica_plan
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
import consumers.registral.parametrica_plan.infrastructure.json.ParametricaPlanImplicits._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.parametrica_plan.projections.ParametricaPlanUpdatedFromDtoProjection

import scala.concurrent.Future
class ParametricaPlanUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ParametricaPlanUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ParametricaPlanUpdatedFromDto"
  override def topicRetry: String = "ParametricaPlanUpdatedFromDto_retry"
  override def topicError: String = "ParametricaPlanUpdatedFromDto_error"

  override def processInput(input: String): Either[Throwable, ParametricaPlanUpdatedFromDto] =
    decode[ParametricaPlanUpdatedFromDto](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ParametricaPlanUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = ParametricaPlanUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
