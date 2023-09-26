package readside.proyectionists.registrales.juicio_obn

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.juicio_obn.domain.JuicioObnEvents.JuicioObnDeletedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success}

class JuicioObnDeletedSnapshotHandler(
                                              implicit
                                              r: MonitoringAndCassandraWrite

                                            ) extends ActorTransaction[JuicioObnDeletedFromDto](r.monitoring)(r.actorTransactionRequirements) {

  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "JuicioObnDeletedFronDto"

  override def topicRetry: String = "JuicioObnDeletedFronDto_retry"

  override def topicError: String = "JuicioObnDeletedFronDto_error"

  import consumers.registral.juicio_obn.infrastructure.json._

  override def processInput(input: String): Either[Throwable, JuicioObnDeletedFromDto] =
    serialization
      .maybeDecode[JuicioObnDeletedFromDto](input)

  override def processMessage(registro: JuicioObnDeletedFromDto): Future[Response.SuccessProcessing] = {



      val cassandra = new CassandraWriteProduction()
      for {
        done <- cassandra
          .cql(
            s"""
          DELETE FROM read_side.buc_juicios_obn """ +
              """ WHERE bju_identificador = """ +
              s""" '${registro.juicioObnId}' """ +
              s""" and bjd_soj_identificador = '${registro.objetoId}' """ +
              s""" and bjd_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
              s""" and bjd_obn_id = '${registro.obligacionId}' """
          )
          .andThen {
            case Failure(exception) => println("Dont persist juicio_obn -1" + exception )
            case Success(_) => {
              println("Persiste juicio_obn -1 ")
            }
          }
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }
}

