package readside.proyectionists.registrales.juicio_tri

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.juicio_tri.domain.JuicioDosEvents.JuicioDosRemovedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.util.{Failure, Success}

class JuicioDosRemovedSnapshotHandler(
                                     implicit r: MonitoringAndCassandraWrite
                                   ) extends ActorTransaction[JuicioDosRemovedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "JuicioDosRemovedSnapshot"

  override def topicRetry: String = "JuicioDosRemovedSnapshot_retry"

  override def topicError: String = "JuicioDosRemovedSnapshot_error"

  import consumers.registral.juicio_tri.infrastructure.json._

  override def processInput(input: String): Either[Throwable, JuicioDosRemovedFromDto] =
    serialization
      .maybeDecode[JuicioDosRemovedFromDto](input)

  override def processMessage(registro: JuicioDosRemovedFromDto): Future[Response.SuccessProcessing] = {
    val cassandra = new CassandraWriteProduction()
    println("VBAJAAAA" )
    for {
      done <- cassandra
        .cql(
          s"""
          DELETE FROM read_side.buc_juicios_cab """ +
            """ WHERE bju_identificador = """ +
            s""" '${registro.juicioId}' """
        )
        .andThen {
          case Failure(exception) => println("Dont persist juicio_tri baja" + exception )
          case Success(_) => {
            println("Persiste juicio_tri baja ")
          }
        }
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}

