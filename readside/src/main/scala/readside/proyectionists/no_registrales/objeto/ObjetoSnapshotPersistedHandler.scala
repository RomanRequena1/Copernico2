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
import readside.proyectionists.no_registrales.objeto.projections.ObjetoPatenteProjection
import readside.proyectionists.no_registrales.objeto.projections.ObjetoDocumentoProjection
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class ObjetoSnapshotPersistedHandler(
    implicit
    r: MonitoringAndCassandraWrite
) extends ActorTransaction[ObjetoSnapshotPersisted](r.monitoring)(r.actorTransactionRequirements) {

  implicit val ec: ExecutionContext = r.actorTransactionRequirements.executionContext
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "ObjetoSnapshotPersistedReadside"
  override def topicRetry: String = "ObjetoSnapshotPersistedReadside_retry"
  override def topicError: String = "ObjetoSnapshotPersistedReadside_error"

  override def processInput(input: String): Either[Throwable, ObjetoSnapshotPersisted] = {
    decode[ObjetoSnapshotPersisted](input)
  }

  override def processMessage(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    registro.operacion match {
      case "U" => handleUpdateOperation(registro)
      case "FD" => handleFocusedDeleteOperation(registro)
      case _ => handleFullDeleteOperation(registro)
    }
  }

  private def handleUpdateOperation(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {

    val mainProjection = ObjetoSnapshotPersistedProjection(registro)

    val tipoObjeto = registro.tipoObjeto.toUpperCase
    val shouldPersistInAdditionalTables = tipoObjeto == "CAM" || tipoObjeto == "NAUT"

    val persistOperations = scala.collection.mutable.ListBuffer[Future[Any]]()

    persistOperations += r.cassandraWrite.writeState(mainProjection).andThen {
      case Failure(exception) =>
        log.error(s"[${registro.objetoId}] Error persisting in main table: ${exception.getMessage}")
      case Success(_) =>
        log.debug(s"[${registro.objetoId}] Successfully persisted in main table")
    }
    if (shouldPersistInAdditionalTables) {
      val patenteProjection = ObjetoPatenteProjection(registro)

      persistOperations += r.cassandraWrite.writeState(patenteProjection).andThen {
        case Failure(exception) =>
          log.error(s"[${registro.objetoId}] Error persisting in patente table: ${exception.getMessage}")
        case Success(_) =>
          log.debug(s"[${registro.objetoId}] Successfully persisted in patente table")
      }

      val documentoProjection = ObjetoDocumentoProjection(registro)

      persistOperations += r.cassandraWrite.writeState(documentoProjection).andThen {
        case Failure(exception) =>
          log.error(s"[${registro.objetoId}] Error persisting in documento table: ${exception.getMessage}")
        case Success(_) =>
          log.debug(s"[${registro.objetoId}] Successfully persisted in documento table")
      }
    }

    Future
      .sequence(persistOperations.toList)
      .map { results =>
        SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
      }
      .recover {
        case ex: Exception =>
          log.error(s"[${registro.objetoId}] Critical error in multi-table persistence", ex)
          throw ex
      }
  }
  private def handleFocusedDeleteOperation(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    val cassandra = new CassandraWriteProduction()

    val patenteValue = extractIdentificador2(registro)
    val documentoValue = extractDocumento(registro)

    log.info(s"[${registro.objetoId}] Iniciando borrado enfocado (FD)")

    val deleteOperations = scala.collection.mutable.ListBuffer[Future[Any]]()

    deleteOperations += cassandra
      .cql(
        s"""DELETE FROM read_side.buc_sujeto_objeto 
         |WHERE soj_suj_identificador = '${registro.sujetoId}' 
         |  AND soj_tipo_objeto = '${registro.tipoObjeto}' 
         |  AND soj_identificador = '${registro.objetoId}'""".stripMargin
      )
      .andThen {
        case Success(_) => log.debug(s"[${registro.objetoId}] ✓ Borrado de tabla principal")
        case Failure(ex) => log.error(s"[${registro.objetoId}] Error borrando de tabla principal", ex)
      }
    patenteValue.filter(_.trim.nonEmpty).foreach { patente =>
      log.info(s"[${registro.objetoId}] Borrando de tabla patente con identificador_2: $patente")
      deleteOperations += cassandra
        .cql(
          s"""DELETE FROM read_side.buc_objeto_patente 
           |WHERE soj_tipo_objeto = '${registro.tipoObjeto}' 
           |  AND soj_identificador_2 = '$patente'
           |  AND soj_identificador = '${registro.objetoId}'
           |  AND soj_suj_identificador = '${registro.sujetoId}'""".stripMargin
        )
        .andThen {
          case Success(_) => log.debug(s"[${registro.objetoId}] ✓ Borrado de tabla patente")
          case Failure(ex) => log.error(s"[${registro.objetoId}] Error borrando de tabla patente", ex)
        }
    }
    documentoValue.filter(_.trim.nonEmpty).foreach { documento =>
      log.info(s"[${registro.objetoId}] Borrando de tabla documento con doc: $documento")
      deleteOperations += cassandra
        .cql(
          s"""DELETE FROM read_side.buc_objeto_documento 
           |WHERE soj_tipo_objeto = '${registro.tipoObjeto}' 
           |  AND soj_documento = '$documento'
           |  AND soj_identificador = '${registro.objetoId}'
           |  AND soj_suj_identificador = '${registro.sujetoId}'""".stripMargin
        )
        .andThen {
          case Success(_) => log.debug(s"[${registro.objetoId}] ✓ Borrado de tabla documento")
          case Failure(ex) => log.error(s"[${registro.objetoId}] Error borrando de tabla documento", ex)
        }
    }

    Future.sequence(deleteOperations.toList).map { _ =>
      log.info(s"[${registro.objetoId}] ✅ Borrado completo de ${deleteOperations.size} tabla(s)")
      SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
    }
  }
  private def handleFullDeleteOperation(registro: ObjetoSnapshotPersisted): Future[Response.SuccessProcessing] = {
    val cassandra = new CassandraWriteProduction()

    log.info(s"[${registro.objetoId}] Ejecutando borrado completo de obligaciones")

    cassandra
      .cql(
        s"""DELETE FROM read_side.buc_obligaciones 
         |WHERE bob_soj_identificador = '${registro.objetoId}' 
         |  AND bob_soj_tipo_objeto = '${registro.tipoObjeto}' 
         |  AND bob_suj_identificador = '${registro.sujetoId}'""".stripMargin
      )
      .map { _ =>
        log.info(s"[${registro.objetoId}] ✅ Obligaciones borradas")
        SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
      }
      .recover {
        case ex: Throwable =>
          log.error(s"[${registro.objetoId}] Error borrando obligaciones", ex)
          throw ex
      }
  }

  private def extractIdentificador2(registro: ObjetoSnapshotPersisted): Option[String] = {
    val value = registro.registro
      .flatMap(_.SOJ_IDENTIFICADOR_2)
      .orElse(registro.objetoId2)

    value.foreach(v => log.debug(s"[${registro.objetoId}] soj_identificador_2 encontrado: $v"))
    value
  }

  private def extractDocumento(registro: ObjetoSnapshotPersisted): Option[String] = {
    val value = registro.registro.flatMap(_.SOJ_DOCUMENTO)

    value.foreach(v => log.debug(s"[${registro.objetoId}] soj_documento encontrado: $v"))
    value
  }
}
