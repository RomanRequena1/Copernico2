package readside.proyectionists.no_registrales.exclusiones_objeto

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.no_registral.exclusiones_objeto.domain.ExclusionesObjetoEvents.ExclusionesObjetoUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.no_registrales.exclusiones_objeto.projections.ExclusionesObjetoUpdatedFromDtoProjection
import consumers.no_registral.exclusiones_objeto.infrastructure.json.ExclusionesObjetoImplicits._
import scala.concurrent.Future


class ExclusionesObjetoUpdatedFromDtoHandler(
                                              implicit
                                              r: MonitoringAndCassandraWrite
                                            ) extends ActorTransaction[ExclusionesObjetoUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ExclusionesObjetoPersistedReadside"
  override def topicRetry: String = "ExclusionesObjetoPersistedReadside_retry"
  override def topicError: String = "ExclusionesObjetoPersistedReadside_error"


  override def processInput(input: String): Either[Throwable, ExclusionesObjetoUpdatedFromDto] = {
    decode[ExclusionesObjetoUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ExclusionesObjetoUpdatedFromDto): Future[Response.SuccessProcessing] = {
    try{
      val projection = ExclusionesObjetoUpdatedFromDtoProjection(registro)
      for {
        done <- cassandra writeState projection
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }catch {
      case e: Exception => println("error cassandra" + e)
    }
    val projection = ExclusionesObjetoUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}