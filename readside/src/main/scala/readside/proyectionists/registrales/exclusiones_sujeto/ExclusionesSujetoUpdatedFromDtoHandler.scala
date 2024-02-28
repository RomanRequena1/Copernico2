package readside.proyectionists.registrales.exclusiones_sujeto

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.exclusiones_sujeto.domain.ExclusionesSujetoEvents.ExclusionesSujetoUpdatedFromDto
import consumers.registral.exclusiones_sujeto.infrastructure.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import readside.proyectionists.registrales.exclusiones_sujeto.projections.ExclusionesSujetoUpdatedFromDtoProjection

import scala.concurrent.Future
class ExclusionesSujetoUpdatedFromDtoHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ExclusionesSujetoUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ExclusionesSujetoUpdatedFromDto"
  override def topicRetry: String = "ExclusionesSujetoUpdatedFromDto_retry"
  override def topicError: String = "ExclusionesSujetoUpdatedFromDto_error"


  override def processInput(input: String): Either[Throwable, ExclusionesSujetoUpdatedFromDto] = {
    decode[ExclusionesSujetoUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()
  override def processMessage(registro: ExclusionesSujetoUpdatedFromDto): Future[Response.SuccessProcessing] = {
    try{
      val projection = ExclusionesSujetoUpdatedFromDtoProjection(registro)
      for {
        done <- cassandra writeState projection
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }catch {
      case e: Exception => println("error cassandra" + e)
    }
    val projection = ExclusionesSujetoUpdatedFromDtoProjection(registro)
    for {
      done <- cassandra writeState projection
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}
