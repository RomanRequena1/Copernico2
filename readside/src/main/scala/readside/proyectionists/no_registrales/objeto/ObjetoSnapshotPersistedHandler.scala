package readside.proyectionists.no_registrales.objeto

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoSnapshotPersisted
import consumers.no_registral.objeto.infrastructure.json.ObjetoImplicits._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser.decode
import org.slf4j.LoggerFactory
import readside.proyectionists.no_registrales.objeto.projections.ObjetoSnapshotPersistedProjection
// AGREGADO: Imports de las nuevas proyecciones
import readside.proyectionists.no_registrales.objeto.projections.ObjetoPatenteProjection
import readside.proyectionists.no_registrales.objeto.projections.ObjetoDocumentoProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}

class ObjetoSnapshotPersistedHandler(
                                      implicit
                                      r: MonitoringAndCassandraWrite
                                    ) extends ActorTransaction[ObjetoSnapshotPersisted](r.monitoring)(r.actorTransactionRequirements) {
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "ObjetoSnapshotPersistedReadside"

  override def topicRetry: String = "ObjetoSnapshotPersistedReadside_retry"

  override def topicError: String = "ObjetoSnapshotPersistedReadside_error"

  override def processInput(input: String): Either[Throwable, ObjetoSnapshotPersisted] = {
    decode[ObjetoSnapshotPersisted](input)
  }

  override def processMessage(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    val multiObjetoANTEnabled: String = Option(System.getenv("MULTI_OBJETO_ANT")).getOrElse("OFF")
    //recordLag(calculateLag(registro.deliveryId.toString))
    val projection: ObjetoSnapshotPersistedProjection = ObjetoSnapshotPersistedProjection(registro)
    if (registro.operacion.equals("U")) {
      // AGREGADO: Verificar si es tipo CAM o NAUT
      val tipoObjeto = registro.tipoObjeto.toUpperCase
      if ((tipoObjeto == "CAM" || tipoObjeto == "NAUT") && multiObjetoANTEnabled.equals("ON")) {
        // Si es CAM o NAUT, persistir en las 3 tablas
        for {
          done <- r.cassandraWrite.writeState(projection).andThen {
            case Failure(exception) => println("Dont persist objeto" + exception)
            case Success(value) => ()
          }
          _ <- r.cassandraWrite.writeState(ObjetoPatenteProjection(registro)).recover { ex: Throwable =>
            println("Error persisting in patente table: " + ex.getMessage)
            ex
          }
          _ <- r.cassandraWrite.writeState(ObjetoDocumentoProjection(registro)).recover { ex: Throwable =>
            println("Error persisting in documento table: " + ex.getMessage)
            ex
          }
        } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
      } else {
        // Tu código original para otros tipos
        for {
          done <- r.cassandraWrite.writeState(projection).andThen {
            case Failure(exception) => println("Dont persist objeto" + exception)
            case Success(value) => ()
            //connOracleReadsideToCass(registro.deliveryId.toString(),"objeto", registro.registro.get.SOJ_CANAL_ORIGEN.getOrElse("TAX"))
          }
        } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
      }
    } else if (registro.operacion.equals("FD")) {
      val cassandra = new CassandraWriteProduction()
      // AGREGADO: Verificar si es tipo CAM o NAUT para borrar de todas las tablas
      val tipoObjeto = registro.tipoObjeto.toUpperCase
      if (tipoObjeto == "CAM" || tipoObjeto == "NAUT") {
        val patenteValue = registro.registro.flatMap(_.SOJ_IDENTIFICADOR_2).orElse(registro.objetoId2).getOrElse("")
        val documentoValue = registro.registro.flatMap(_.SOJ_DOCUMENTO).getOrElse("")

        for {
          done <- cassandra
            .cql(
              s"""
            DELETE FROM read_side.buc_sujeto_objeto """ +
                s""" WHERE soj_suj_identificador = '${registro.sujetoId}' """ +
                s""" and soj_tipo_objeto = '${registro.tipoObjeto}' """ +
                s""" and soj_identificador = '${registro.objetoId}' """
            )
            .recover { ex: Throwable =>
              log.error("ERROR DELETE OBJETO:  " + ex.getMessage)
              ex
            }
          _ <- cassandra.cql(
            s"""DELETE FROM read_side.buc_objeto_patente
               |WHERE soj_tipo_objeto = '${registro.tipoObjeto}'
               |  AND soj_identificador_2 = '$patenteValue'
               |  AND soj_identificador = '${registro.objetoId}'
               |  AND soj_suj_identificador = '${registro.sujetoId}'""".stripMargin
          ).recover { ex: Throwable =>
            log.error("ERROR DELETE OBJETO PATENTE:  " + ex.getMessage)
            ex
          }
          _ <- cassandra.cql(
            s"""DELETE FROM read_side.buc_objeto_documento
               |WHERE soj_tipo_objeto = '${registro.tipoObjeto}'
               |  AND soj_documento = '$documentoValue'
               |  AND soj_identificador = '${registro.objetoId}'
               |  AND soj_suj_identificador = '${registro.sujetoId}'""".stripMargin
          ).recover { ex: Throwable =>
            log.error("ERROR DELETE OBJETO DOCUMENTO:  " + ex.getMessage)
            ex
          }
        } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
      } else {
        // Tu código original para otros tipos
        for {
          done <- cassandra
            .cql(
              s"""
            DELETE FROM read_side.buc_sujeto_objeto """ +
                """ WHERE soj_suj_identificador = """ +
                s""" '${registro.sujetoId}' """ +
                s""" and soj_tipo_objeto = '${registro.tipoObjeto}' """ +
                s""" and soj_identificador = '${registro.objetoId}' """
            )
            .recover { ex: Throwable =>
              log.error("ERROR DELETE OBJETO:  " + ex.getMessage)
              ex
            }
        } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
      }
    } else {
      val cassandra = new CassandraWriteProduction()
      for {
        // FULL DELETE
        done <- cassandra
          .cql(
            s"""
            DELETE FROM read_side.buc_obligaciones """ +
              s""" WHERE bob_soj_identificador = '${registro.objetoId}' """ +
              s""" and bob_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
              s""" and bob_suj_identificador = '${registro.sujetoId}' """
          )
          .recover { ex: Throwable =>
            log.error("ERROR DELETE OBLIGACION FROM OBJETO:  " + ex.getMessage)
            ex
          }
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }
  }
}