package readside.proyectionists.registrales.componente_i

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.registral.componente_i.domain.ComponenteIEvents.ComponenteIPersistedSnapshot
import consumers.registral.componente_i.infrastructure.json.json._
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.componente_i.projectionists.ComponenteISnapshotProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}
class ComponenteIPersistedSnapshotHandler(
                                          implicit
                                          r: MonitoringAndCassandraWrite
                                        ) extends ActorTransaction[ComponenteIPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {
  @JsonIgnore
  private val log = LoggerFactory.getLogger(this.getClass)
  override def topic: String = "ComponenteIPersistedSnapshot"

  override def topicRetry: String = "ComponenteIPersistedSnapshot_retry"

  override def topicError: String = "ComponenteIPersistedSnapshot_error"
  override def processInput(input: String): Either[Throwable, ComponenteIPersistedSnapshot] =
    decode[ComponenteIPersistedSnapshot](input)
  override def processMessage(registro: ComponenteIPersistedSnapshot): Future[Response.SuccessProcessing] = {
    //log.error("llego event")
    //recordLag(calculateLag(registro.deliveryId.toString))

      val projection = ComponenteISnapshotProjection(registro)

      /*r.cassandraWrite.writeState(projection).onComplete {
        case Failure(exception) => log.error(exception)

        case Success(value) =>

          connOracleReadsideToCass(registro.sujetoId,registro.tipoObjeto,registro.objetoId,registro.obligacionId)
          SuccessProcessing(registro.aggregateRoot, registro.deliveryId)

      }*/

      for {
        done <- r.cassandraWrite.writeState(projection).andThen {
          case Failure(exception) => log.error("Dont persist cupon" + exception )
          case Success(value) => {
            //log.error("ERROR - 1 " + registro.deliveryId)
            log.debug("Persist cupon" + value)
            //connOracleReadsideToCass(registro.deliveryId.toString(), "obligacion", registro.registro.get.BOB_CANAL_ORIGEN.getOrElse("TAX"))
          }
        }


        /*recover { ex: Throwable =>
        connOracleReadsideToCass(registro.sujetoId,registro.tipoObjeto,registro.objetoId,registro.obligacionId)
        log.error(ex.getMessage)
        log.error("readside oracle")
        ex
      }*/
      } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)

  }

}
