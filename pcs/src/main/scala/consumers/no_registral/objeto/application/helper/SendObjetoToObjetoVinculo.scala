package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.{ObjetoRemoveObligacion, ObjetoUpdateFromObligacion, ObjetoUpdateFromObnTreintaPorciento, ObjetoUpdateFromTri}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.{CreateTransfVinculoObjetoFromObj, RemoveObjetoVinculo, UpdateVinculoObjetoFromObj}
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage.ObjetoVinculoMessageRoots
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.actor_model.Response
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}


//todo --------------------------- REFACTORIZAR ----------------------------------
object SendObjetoToObjetoVinculo {
  implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
  val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"))
  /**
   * 1. Crear actor
   * Si el actor ya existe, se envía el mensaje al actor existente que entra por el catch y si no existe se crea el actor
   * Si estado es TRANSF se envia el mensaje CreateTransfVinculoObjetoFromObj y se hace los informes hacia el padre
   * Si estado no es TRANSF se envia el mensaje UpdateVinculoObjetoFromObj
   */
  protected val log = LoggerFactory.getLogger(this.getClass)

  def apply(actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer): Unit = {

    log.error("CUMBIA SendObjetoToObjetoVinculo  -> ")
    val objetoVinculoMessageRoots = ObjetoVinculoMessageRoots(objetoId).toString
    //implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    val actorPath = s"akka://PersonClassificationService/system/sharding/SujetoActor/*/${sujetoId}/Sujeto-${sujetoId}-Objeto-${objetoId}-${tipoObjeto}/ObjetoVinculo-${objetoId}"
    // s"akka://PersonClassificationService/user/ObjetoVinculo-${objetoId}"


    actor.context.child(objetoVinculoMessageRoots) match {



      case Some(value) => {

        Future(actor.context.stop(value)).onComplete {
          case Failure(exception) => log.error(s"Hubo un error al stopear el actor: ${exception.getMessage}" + )
          case Success(value) => {
            //todo cambiar
            val actorTry = Try {
              implicit val actorObjetoVinculo: ActorRef = actor.context.actorOf(actorProp, objetoVinculoMessageRoots)
              println("CUMBIAAAA ::::::::::::::::: actor::::::::::::::::::::: " + actorObjetoVinculo.path + " - ")
              actorObjetoVinculo
            }
            actorTry match {
              case Success(actorObjetoVinculo) =>
                estado match {
                  case x if x.getOrElse("").equals("TRANSF") =>
                    log.error("CREO TRANSF VINCULOOBJETO" + sujetoId)
                    actorObjetoVinculo ! CreateTransfVinculoObjetoFromObj(0,
                      sujetoId,
                      objetoId,
                      tipoObjeto,
                      actor.state.tiene30Objeto,
                      Some(actor.state.isResponsable),
                      actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                      actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
                    )
                    if (actor.state.tiene30Objeto.equals(false))
                      actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
                    else
                      actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)

                  case x if x.getOrElse("").equals("BAJA") =>
                    log.error("ELIMINO VINCOBJETO" + sujetoId)
                    actorObjetoVinculo ! RemoveObjetoVinculo(0,
                      sujetoId,
                      objetoId,
                      tipoObjeto,
                      actor.state.tiene30Objeto,
                      Some(actor.state.isResponsable),
                      actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                      actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
                  case _ =>
                    log.error("CREO UPDATEVINCULOOBJETO" + sujetoId)
                    actorObjetoVinculo ! UpdateVinculoObjetoFromObj(0,
                      sujetoId,
                      objetoId,
                      tipoObjeto,
                      actor.state.tiene30Objeto,
                      Some(actor.state.isResponsable),
                      actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                      actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
                    )
                }

              case Failure(exception) => log.error(s"Hubo un error al crear el actor: ${exception.getMessage}")
            }
          }
        }
      }
      case None => {
        val actorTry = Try {
          implicit val actorObjetoVinculo: ActorRef = actor.context.actorOf(actorProp, objetoVinculoMessageRoots)
          println("CUMBIAAAA ::::::::::::::::: actor::::::::::::::::::::: " + actorObjetoVinculo.path + " - ")
          actorObjetoVinculo
        }
        actorTry match {
          case Success(actorObjetoVinculo) =>
            estado match {
              case x if x.getOrElse("").equals("TRANSF") =>
                log.error("CREO TRANSF VINCULOOBJETO" + sujetoId)
                actorObjetoVinculo ! CreateTransfVinculoObjetoFromObj(0,
                  sujetoId,
                  objetoId,
                  tipoObjeto,
                  actor.state.tiene30Objeto,
                  Some(actor.state.isResponsable),
                  actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                  actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
                )
                if (actor.state.tiene30Objeto.equals(false))
                  actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
                else
                  actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)

              case x if x.getOrElse("").equals("BAJA") =>
                log.error("ELIMINO VINCOBJETO" + sujetoId)
                actorObjetoVinculo ! RemoveObjetoVinculo(0,
                  sujetoId,
                  objetoId,
                  tipoObjeto,
                  actor.state.tiene30Objeto,
                  Some(actor.state.isResponsable),
                  actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                  actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
              case _ =>
                log.error("CREO UPDATEVINCULOOBJETO" + sujetoId)
                actorObjetoVinculo ! UpdateVinculoObjetoFromObj(0,
                  sujetoId,
                  objetoId,
                  tipoObjeto,
                  actor.state.tiene30Objeto,
                  Some(actor.state.isResponsable),
                  actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
                  actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
                )
            }

          case Failure(exception) => log.error(s"Hubo un error al crear el actor: ${exception.getMessage}")
        }
      }
    }
  }
}