package consumers.registral.objeto_juicio.infrastructure.projection

import akka.Done
import akka.actor.typed.ActorSystem
import akka.projection.eventsourced.EventEnvelope
import akka.projections.ProjectionSettings
import akka.projections.cassandra.CassandraProjectionHandler
import akka.util.Timeout
import cassandra.write.CassandraWriteProduction
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioQueries.GetStateObjetoJuicio
import consumers.registral.objeto_juicio.application.entities.ObjetoJuicioResponses.GetObjetoJuicioResponse
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents
import consumers.registral.objeto_juicio.domain.ObjetoJuicioEvents.{ObjetoJuicioRemovedFromDto, ObjetoJuicioUpdatedFromDto}
import consumers.registral.objeto_juicio.infrastructure.dependency_injection.ObjetoJuicioActor
import consumers.registral.objeto_juicio.infrastructure.projection.schemas.ObjetoJuicioSnapshotProjection
import design_principles.actor_model.mechanism.AbstractOverReplyTo.MessageWithAutomaticReplyTo

import scala.concurrent.Future
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

class ObjetoJuicioProjectionHandler(settings: ProjectionSettings, system: ActorSystem[_], actor: ObjetoJuicioActor) extends CassandraProjectionHandler[ObjetoJuicioEvents](settings, system) {
  def event: ObjetoJuicioUpdatedFromDto = null

  def bindings: List[(String, Any)] = null

  val cas = new CassandraWriteProduction
  implicit val contexto = (system.executionContext)
  implicit val timeout: Timeout = 30 seconds


  override def processMessage(envelope: EventEnvelope[ObjetoJuicioEvents]): Future[Done] = {
    log.info("Llego evento")
    envelope.event match {
//      case event: ObligacionPersistedSnapshot => {
//        println("Debe Persistir " + event.deliveryId)
//        Future.successful(Done)
//      }
      case event: ObjetoJuicioUpdatedFromDto => {

        /*
        ObligacionPersistedSnapshot(
          deliveryId = lastDeliveryId,
          sujetoId = evt.sujetoId,
          objetoId = evt.objetoId,
          tipoObjeto = evt.tipoObjeto,
          obligacionId = evt.obligacionId,
          registro = state.registro,
          exenta = state.exenta,
          porcentajeExencion = state.porcentajeExencion.getOrElse(0),
          saldo = state.saldo.getOrElse(0),
          operacion = ObligacionEvents.operaciones("Upsert"),
          resultDmn = state.resultDmn
        )
        */
        log.info("Proyeccion: " + event.deliveryId)
        val preg = actor.getEntityRefTyped[GetStateObjetoJuicio, GetObjetoJuicioResponse](event.aggregateRoot)

        preg.ask[GetObjetoJuicioResponse](ref => MessageWithAutomaticReplyTo(
          GetStateObjetoJuicio(event.objetoId,
                               event.tipoObjeto,
                               event.idRel,
                               event.tipoObjetoRel),
          ref
        )) onComplete {
          case Success(value: GetObjetoJuicioResponse) => {
            println("Value: " + value.state.toString)
            val miSnapshot = ObjetoJuicioUpdatedFromDto(
              objetoId = event.objetoId,
              tipoObjeto = event.tipoObjeto,
              idRel = event.idRel,
              tipoObjetoRel = event.tipoObjetoRel,
              tipoRel = value.state.get.registro.get.RJP_TIPO_REL,
              idExterno = value.state.get.registro.get.RJP_ID_EXTERNO,
              idExterno2 = value.state.get.registro.get.RJP_ID_EXTERNO_2,
              estado = value.state.get.registro.get.RJP_ESTADO,
              deliveryId = value.lastDeliveryIdByEvent,
              registro = value.state.get.registro.get
            )
            println("SS: " + miSnapshot.toString)
            cas.writeState(ObjetoJuicioSnapshotProjection(miSnapshot))
          }
          case Failure(exception) => log.error("Error Projection: " + exception.getMessage)
        }

//        val event1 = ObligacionPersistedSnapshot(
//          deliveryId = event.deliveryId,
//          sujetoId = event.sujetoId,
//          objetoId = event.objetoId,
//          tipoObjeto = event.tipoObjeto,
//          obligacionId = event.obligacionId,
//          registro = Some(event.registro),
//          exenta = state.exenta,
//          porcentajeExencion = state.porcentajeExencion.getOrElse(0),
//          saldo = state.saldo.getOrElse(0),
//          operacion = ObligacionEvents.operaciones("Upsert"),
//          resultDmn = state.resultDmn
//        )

        Future.successful(Done)
      }

      case event: ObjetoJuicioRemovedFromDto => {
        log.info("Proyeccion: " + event.deliveryId)
        Future.successful(Done)
      }
      case _ => {
        log.info("Otro evento...")
        Future.successful(Done)
      }
    }

  }
}