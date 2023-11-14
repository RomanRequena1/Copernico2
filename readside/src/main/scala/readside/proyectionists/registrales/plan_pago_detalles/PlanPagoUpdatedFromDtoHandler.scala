package readside.proyectionists.registrales.plan_pago_detalles
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.plan_pago_detalles.domain.PlanPagoEvents.PlanPagoUpdatedFromDto
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import readside.proyectionists.registrales.plan_pago_detalles.projections.PlanPagoUpdatedFromDtoProjection
import consumers.registral.plan_pago_detalles.infrastructure.json.json._
import io.circe.parser._
class PlanPagoUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[PlanPagoUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "PlanPagoObnUpdatedFromDto"
  override def topicRetry: String = "PlanPagoObnUpdatedFromDto_retry"
  override def topicError: String = "PlanPagoObnUpdatedFromDto_error"

  override def processInput(input: String): Either[Throwable, PlanPagoUpdatedFromDto] = {
    decode[PlanPagoUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: PlanPagoUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = PlanPagoUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
