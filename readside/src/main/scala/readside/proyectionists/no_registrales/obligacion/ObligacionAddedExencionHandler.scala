package readside.proyectionists.no_registrales.obligacion
import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionAddedExencion
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser.decode
import org.slf4j.LoggerFactory

import scala.concurrent.Future
class ObligacionAddedExencionHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObligacionAddedExencion](r.monitoring)(r.actorTransactionRequirements) {

  override def topic: String = "ObligacionAddedExencion"

  override def topicRetry: String = "ObligacionAddedExencion_retry"

  override def topicError: String = "ObligacionAddedExencion_error"

  override def processInput(input: String): Either[Throwable, ObligacionAddedExencion] =
    decode[ObligacionAddedExencion](input)

  val cassandra = new CassandraWriteProduction()
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)
  override def processMessage(registro: ObligacionAddedExencion): Future[Response.SuccessProcessing] = {
    for {
      done <- cassandra
        .cql(
          s"""
          DELETE FROM read_side.buc_obligaciones """ +
          """ WHERE bob_suj_identificador = """ +
          s""" '${registro.sujetoId}' """ +
          s""" and bob_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
          s""" and bob_soj_identificador = '${registro.objetoId}' """ +
          s""" and bob_obn_id = '${registro.obligacionId}'
          """
        )
        .recover { ex: Throwable =>
          log.error(ex.getMessage)
          ex
        }
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }

}
