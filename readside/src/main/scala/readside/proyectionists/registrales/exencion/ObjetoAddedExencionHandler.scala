package readside.proyectionists.registrales.exencion
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoAddedExencion
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.exencion.projections.ObjetoAddedExencionProjection

import scala.concurrent.Future
class ObjetoAddedExencionHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObjetoAddedExencion](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObjetoAddedExencion"
  override def topicRetry: String = "ObjetoAddedExencion_retry"
  override def topicError: String = "ObjetoAddedExencion_error"

  import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._

  override def processInput(input: String): Either[Throwable, ObjetoAddedExencion] =
    decode[ObjetoAddedExencion](input)

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ObjetoAddedExencion): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection = ObjetoAddedExencionProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
