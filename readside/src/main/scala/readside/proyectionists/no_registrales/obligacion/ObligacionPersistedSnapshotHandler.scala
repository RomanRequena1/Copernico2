package readside.proyectionists.no_registrales.obligacion

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.obligacion.domain.ObligacionEvents.ObligacionPersistedSnapshot
import consumers.no_registral.obligacion.infrastructure.json.ObligacionImplicits._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser.decode
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.obligacion.projectionists.ObligacionSnapshotProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ObligacionPersistedSnapshotHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObligacionPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "ObligacionPersistedSnapshot"

  override def topicRetry: String = "ObligacionPersistedSnapshot_retry"

  override def topicError: String = "ObligacionPersistedSnapshot_error"
  override def processInput(input: String): Either[Throwable, ObligacionPersistedSnapshot] =
    decode[ObligacionPersistedSnapshot](input)

  val contadorBien = r.monitoring.counter("Mi_prueba_readside")
  val contadorMal = r.monitoring.counter("Mi_prueba_readside_fail")
  override def processMessage(registro: ObligacionPersistedSnapshot): Future[Response.SuccessProcessing] = {
    //recordLag(calculateLag(registro.deliveryId.toString))
    if (registro.operacion.equals("U")) {
      val projection = ObligacionSnapshotProjection(registro)

      for {
        done <- r.cassandraWrite.writeState(projection).andThen {
          case Failure(exception) => {
            log.error("Dont persist obligacion" + exception)
            contadorMal.increment()
          }
          case Success(value) => {
            contadorBien.increment()
          }
        }
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    } else {

      val cassandra = new CassandraWriteProduction()
      val bco = registro.registro match {
        case Some(x) => x.BOB_CANAL_ORIGEN.getOrElse("TAX")
        case None => "TAX"
      }
      //TODO pk:  si cambia la pk de la tabla, reordenar
      for {
        done <- cassandra
          .cql(
            s"""
      DELETE FROM read_side.buc_obligaciones """ +
            s""" WHERE bob_soj_identificador = '${registro.objetoId}' """ +
            s""" and bob_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
            s""" and bob_suj_identificador = '${registro.sujetoId}' """ +
            s""" and bob_obn_id = '${registro.obligacionId}' """
          )
          .andThen {
            case Failure(exception) => log.error("Dont persist obligacion" + exception)
            case Success(_) => ()
          }
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }
  }
}
