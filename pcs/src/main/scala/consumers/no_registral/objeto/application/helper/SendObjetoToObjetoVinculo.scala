package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.{CreateTransfVinculoObjetoFromObj, RemoveObjetoVinculo, UpdateVinculoObjetoFromObj}
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.actor_model.{Command, Response}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}


//todo --------------------------- REFACTORIZAR ----------------------------------
object SendObjetoToObjetoVinculo {
  //FIXME: revisar obj_default si es necesario y que deliveryId tendria
  val obj_default: ObjetosTri = ObjetosTri(Some("None"), 99, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"),None,None)
  /**
   * 1. Crear actor
   * Si el actor ya existe, se envía el mensaje al actor existente que entra por el catch y si no existe se crea el actor
   * Si estado es TRANSF se envia el mensaje CreateTransfVinculoObjetoFromObj y se hace los informes hacia el padre
   * Si estado no es TRANSF se envia el mensaje UpdateVinculoObjetoFromObj
   */

  def apply(vinculoActor: ActorRef, actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer, command: Command): Unit = {
    if(vinculoActor.path.toString.equals("akka://PersonClassificationService/system/sharding/ObjetoVinculoActor")){

      testIfObjVinculo(vinculoActor, actor, sujetoId, objetoId, tipoObjeto, estado, requeriment, command)
    } else {
      implicit val ac: ActorSystem = actor.context.system
      val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
      testIfObjVinculo(vinculoActor, actor, sujetoId, objetoId, tipoObjeto, estado, requeriment, command)

    }
  }
}

object testIfObjVinculo {

  def apply(vinculoActor: ActorRef, actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer, command: Command): Unit = {
    val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"), Some("None"),None,None)

    val log: Logger = LoggerFactory.getLogger(this.getClass)
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    estado match {
      case x if x.getOrElse("").equals("TRANSF") =>
        val res = vinculoActor.ask[Response.SuccessProcessing](CreateTransfVinculoObjetoFromObj(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = 0,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto
        ))
        res.onComplete {
          case Failure(exception) => log.error("Error to send event to objeto_vinculo (TRANSF)" + exception + "objID: "+ objetoId + "sujID: "+sujetoId)
          case Success(value) => log.debug("Sent event to objet_vinculo (TRANSF)" + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }

        if (actor.state.tiene30Objeto.equals(false))
          actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
        else
          actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)

        //FIXME: Objeto en baja recibe una obn, activa el VSO?
      case x if x.getOrElse("").equals("BAJA") =>
        val res = vinculoActor.ask[Response.SuccessProcessing](RemoveObjetoVinculo(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = 0,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto))
        res.onComplete {
          case Failure(exception) => log.error("Error to send event to objeto_vinculo (BAJA) " + exception + " objID: "+ objetoId + "sujID: "+sujetoId)
          case Success(value) => log.debug("Sent event to objet_vinculo (BAJA) " + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }

      case _ =>
        val res = vinculoActor.ask[Response.SuccessProcessing](UpdateVinculoObjetoFromObj(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = 0,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto))
        res.onComplete {
          case Failure(exception) => log.error("Error to send event to objeto_vinculo (case_) " + exception + " objID: "+ objetoId + " sujID: "+sujetoId)
          case Success(value) => log.debug("Sent event to objet_vinculo (case_)" + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }
    }
  }

}