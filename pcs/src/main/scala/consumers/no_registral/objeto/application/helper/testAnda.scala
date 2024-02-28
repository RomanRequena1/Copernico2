package consumers.no_registral.objeto.application.helper

import akka.actor.Props
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.{CreateTransfVinculoObjetoFromObj, RemoveObjetoVinculo, UpdateVinculoObjetoFromObj}
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage.ObjetoVinculoMessageRoots
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success, Try}

object testAnda {
      def apply(actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer): Unit = {
        implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
        val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"))

         val log = LoggerFactory.getLogger(this.getClass)

        val objetoVinculoMessageRoots = ObjetoVinculoMessageRoots(objetoId).toString
        //implicit val system: ActorSystem = actor.context.system
        implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)

        val actorTry = Try {
          implicit val ac = actor.context.system
          val Obje = ObjetoVinculoActor.startWithRequirements(requeriment)
          Obje
//          implicit val actorObjetoVinculo: ActorRef = actor.context.actorOf(actorProp, objetoVinculoMessageRoots)
//          println("CUMBIAAAA ::::::::::::::::: actor::::::::::::::::::::: " + actorObjetoVinculo.path + " - ")
//          actorObjetoVinculo
        }


        actorTry match {
          case Success(actorObjetoVinculo) =>
            estado match {
              case x if x.getOrElse("").equals("TRANSF") =>
                actorObjetoVinculo ! CreateTransfVinculoObjetoFromObj(objetoId = objetoId,
                  sujetoId = sujetoId,
                  deliveryId = 0,
                  tipoObj = tipoObjeto,
                  tiene30Objeto = actor.state.tiene30Objeto,
                  isResponsable = Some(actor.state.isResponsable),
                  estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                  titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
                )
                if (actor.state.tiene30Objeto.equals(false))
                  actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
                else
                  actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)

              case x if x.getOrElse("").equals("BAJA") =>
                actorObjetoVinculo ! RemoveObjetoVinculo(
                  objetoId = objetoId,
                  sujetoId = sujetoId,
                  deliveryId = 0,
                  tipoObj = tipoObjeto,
                  tiene30Objeto = actor.state.tiene30Objeto,
                  isResponsable = Some(actor.state.isResponsable),
                  estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                  titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
              case _ =>
                actorObjetoVinculo ! UpdateVinculoObjetoFromObj(objetoId = objetoId,
                  sujetoId = sujetoId,
                  deliveryId = 0,
                  tipoObj = tipoObjeto,
                  tiene30Objeto = actor.state.tiene30Objeto,
                  isResponsable = Some(actor.state.isResponsable),
                  estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                  titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
            }

          case Failure(exception) => log.error(s"Hubo un error al crear el actor: ${exception.getMessage}")
        }
      }

}
