package readside.proyectionists.registrales.subasta
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.subasta.domain.SubastaEvents.SubastaUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.subasta.projections.SubastaUpdatedFromDtoProjection

import scala.concurrent.Future
class SubastaUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[SubastaUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "SubastaUpdatedFromDto"
  override def topicError: String = "SubastaUpdatedFromDto_error"
  override def topicRetry: String = "SubastaUpdatedFromDto_retry"

  import consumers.registral.subasta.infrastructure.json.json._

  override def processInput(input: String): Either[Throwable, SubastaUpdatedFromDto] =
    decode[SubastaUpdatedFromDto](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: SubastaUpdatedFromDto): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = SubastaUpdatedFromDtoProjection(registro)
    projection.updateReadside()
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
