package readside.proyectionists.registrales.objeto_juicio

import akka.entity.ShardedEntity.MonitoringAndCassandraWrite
import api.actor_transaction.ActorTransaction
import cassandra.write.CassandraWriteProduction
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.ObjetoJuicioRemovedFromDto
import design_principles.actor_model.Response
import design_principles.actor_model.Response.SuccessProcessing
import io.circe.parser._
import org.slf4j.LoggerFactory
import scala.concurrent.Future
import scala.util.{Failure, Success}

class ObjetoJuicioRemovedFromDtoHandler(
                                         implicit
                                         r: MonitoringAndCassandraWrite
                                       ) extends ActorTransaction[ObjetoJuicioRemovedFromDto](r.monitoring)(r.actorTransactionRequirements) {


  private val log = LoggerFactory.getLogger(this.getClass)

  override def topic: String = "ObjetoJuicioRemovedFromDto"

  override def topicRetry: String = "ObjetoJuicioRemovedFromDto_retry"

  override def topicError: String = "ObjetoJuicioRemovedFromDto_error"

  import consumers.registral.objeto_juicio.infrastructure.json._

  override def processInput(input: String): Either[Throwable, ObjetoJuicioRemovedFromDto] = {
    decode[ObjetoJuicioRemovedFromDto](input)
  }


  override def processMessage(registro: ObjetoJuicioRemovedFromDto): Future[Response.SuccessProcessing] = {

    val cassandra = new CassandraWriteProduction()
    for {
      done <- cassandra
        .cql(
          s"""
           DELETE FROM read_side.buc_objeto_rel """ +
            s""" WHERE rjp_soj_identificador = '${registro.objetoId}' """ +
            s""" and rjp_soj_tipo_objeto = '${registro.tipoObjeto}' """ +
            s""" and rjp_identificador_rel = '${registro.idRel}' """ +
            s""" and rjp_tipo_objeto_rel = '${registro.tipoObjetoRel}' """

        ).andThen{
          case Failure(exception) => log.error("Dont persist objeto-juicio_obn BAJA" + exception )
          case Success(_) => {
            log.debug("Persist objeto-juicio_obn BAJA")
          }
        }
    } yield SuccessProcessing(registro.aggregateRoot, registro.deliveryId)
  }
}
