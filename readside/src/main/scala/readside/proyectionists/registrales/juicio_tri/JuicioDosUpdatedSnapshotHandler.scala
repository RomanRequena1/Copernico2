package readside.proyectionists.registrales.juicio_tri

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosUpdatedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import readside.proyectionists.registrales.juicio_tri.projections.JuicioDosUpdatedFromDtoProjection
import consumers.registral.juicio_tri.infrastructure.json.json._
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.util.{Failure, Success}
import io.circe.parser._
class JuicioDosUpdatedSnapshotHandler(
                                       implicit
                                       r: MonitoringAndCassandraWrite
                                     ) extends ActorTransaction[JuicioDosUpdatedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "JuicioDosPersistedSnapshot"

  override def topicRetry: String = "JuicioDosPersistedSnapshot_retry"

  override def topicError: String = "JuicioDosPersistedSnapshot_error"


  override def processInput(input: String): Either[Throwable, JuicioDosUpdatedFromDto] = {
    decode[JuicioDosUpdatedFromDto](input)
  }

  val cassandra = new CassandraWriteProduction()

  override def processMessage(registro: JuicioDosUpdatedFromDto): Future[Response.SuccessProcessing] = {
    val projection = JuicioDosUpdatedFromDtoProjection(registro)
    for {
      done <- r.cassandraWrite.writeState(projection)
        .andThen {
          case Failure(exception) => println("Dont persist juicio_tri" + exception)
          case Success(value) => {
            println("Persist juicio_tri" + value)
          }
        }
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}


