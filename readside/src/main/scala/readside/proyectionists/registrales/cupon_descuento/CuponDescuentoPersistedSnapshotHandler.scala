package readside.proyectionists.registrales.cupon_descuento

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import consumers.registral.cupon_descuento.domain.CuponDescuentoEvents.CuponDescuentoPersistedSnapshot
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import org.slf4j.LoggerFactory
import readside.proyectionists.registrales.cupon_descuento.projectionists.CuponDescuentoSnapshotProjection

import scala.concurrent.Future
import scala.util.{Failure, Success}

class CuponDescuentoPersistedSnapshotHandler(
                                          implicit
                                          r: MonitoringAndCassandraWrite
                                        ) extends ActorTransaction[CuponDescuentoPersistedSnapshot](r.monitoring)(r.actorTransactionRequirements) {

  private val log = LoggerFactory.getLogger(this.getClass)
  log.error("cumbia 3")
  override def topic: String = "CuponDescuentoPersistedSnapshot"

  override def topicRetry: String = "CuponDescuentoPersistedSnapshot_retry"

  override def topicError: String = "CuponDescuentoPersistedSnapshot_error"

  import consumers.registral.cupon_descuento.infrastructure.json._
  override def processInput(input: String): Either[Throwable, CuponDescuentoPersistedSnapshot] =
    serialization
      .maybeDecode[CuponDescuentoPersistedSnapshot](input)

  override def processMessage(registro: CuponDescuentoPersistedSnapshot): Future[Response.SuccessProcessing] = {
    //log.error("llego event")
    //recordLag(calculateLag(registro.deliveryId.toString))

      val projection = CuponDescuentoSnapshotProjection(registro)

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
