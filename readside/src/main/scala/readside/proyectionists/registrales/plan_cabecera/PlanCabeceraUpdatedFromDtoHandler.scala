package readside.proyectionists.registrales.plan_cabecera
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite

import scala.concurrent.Future
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.plan_cabecera.domain.PlanCabeceraEvents.PlanCabeceraUpdatedFromDto
import design_principles.actor_model.Response.SuccessProcessing
import design_principles.actor_model.Response
import consumers.registral.plan_cabecera.infrastructure.json.json._
import io.circe.parser._
import readside.proyectionists.registrales.plan_cabecera.projections.PlanCabeceraUpdatedFromDtoProjection
class PlanCabeceraUpdatedFromDtoHandler(
                                         implicit
                                         r: MonitoringAndCassandraWrite
) extends ActorTransaction[PlanCabeceraUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "PlanCabeceraUpdatedFromDto"
  override def topicRetry: String = "PlanCabeceraUpdatedFromDto_retry"
  override def topicError: String = "PlanCabeceraUpdatedFromDto_error"

  override def processInput(input: String): Either[Throwable, PlanCabeceraUpdatedFromDto] = {
    decode[PlanCabeceraUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: PlanCabeceraUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = PlanCabeceraUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
