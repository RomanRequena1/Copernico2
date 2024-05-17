package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.{CreateTransfVinculoObjetoFromObj, RemoveObjetoVinculo, UpdateVinculoObjetoFromObj}
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.actor_model.Response
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}


//todo --------------------------- REFACTORIZAR ----------------------------------
object SendObjetoToObjetoVinculo {
  val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"))
  /**
   * 1. Crear actor
   * Si el actor ya existe, se envía el mensaje al actor existente que entra por el catch y si no existe se crea el actor
   * Si estado es TRANSF se envia el mensaje CreateTransfVinculoObjetoFromObj y se hace los informes hacia el padre
   * Si estado no es TRANSF se envia el mensaje UpdateVinculoObjetoFromObj
   */

  def apply(Obje: ActorRef,actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer): Unit = {
    //implicit val system: ActorSystem = actor.context.system


    val actorPath = s"akka://PersonClassificationService/system/sharding/SujetoActor/*/${sujetoId}/Sujeto-${sujetoId}-Objeto-${objetoId}-${tipoObjeto}/ObjetoVinculo-${objetoId}"
    // s"akka://PersonClassificationService/user/ObjetoVinculo-${objetoId}"



    //          implicit val actorObjetoVinculo: ActorRef = actor.context.actorOf(actorProp, objetoVinculoMessageRoots)
    //          println("CUMBIAAAA ::::::::::::::::: actor::::::::::::::::::::: " + actorObjetoVinculo.path + " - ")
    //          actorObjetoVinculo



    // akka://PersonClassificationService/system/sharding/ObjetoVinculoActor
    if(Obje.path.toString.equals("akka://PersonClassificationService/system/sharding/ObjetoVinculoActor")){

      testIfObjVinculo(Obje, actor, sujetoId, objetoId, tipoObjeto, estado, requeriment)
    } else {
      implicit val ac: ActorSystem = actor.context.system
      val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
      testIfObjVinculo(Obje, actor, sujetoId, objetoId, tipoObjeto, estado, requeriment)

    }
  }
}

object testIfObjVinculo {

  def apply(Obje: ActorRef,actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer): Unit = {
    val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"), Some("None"))
    val log: Logger = LoggerFactory.getLogger(this.getClass)
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    estado match {
      case x if x.getOrElse("").equals("TRANSF") =>
        val res = Obje.ask[Response.SuccessProcessing](CreateTransfVinculoObjetoFromObj(objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = 0,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
        ))
        res.onComplete {
          case Failure(exception) => log.error("Error to send event to objeto_vinculo (TRANSF)" + exception + "objID: "+ objetoId + "sujID: "+sujetoId)
          case Success(value) => log.debug("Sent event to objet_vinculo " + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }

        if (actor.state.tiene30Objeto.equals(false))
          actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
        else
          actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)

      case x if x.getOrElse("").equals("BAJA") =>
        val res = Obje.ask[Response.SuccessProcessing](RemoveObjetoVinculo(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = 0,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD))
        res.onComplete {
          case Failure(exception) => log.error("Error to send event to objeto_vinculo (BAJA) " + exception + " objID: "+ objetoId + "sujID: "+sujetoId)
          case Success(value) => log.debug("Sent event to objet_vinculo " + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }

      case _ =>
        val res = Obje.ask[Response.SuccessProcessing](UpdateVinculoObjetoFromObj(objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = 0,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD))
        res.onComplete {
          case Failure(exception) => log.error("Error to send event to objeto_vinculo " + exception + " objID: "+ objetoId + " sujID: "+sujetoId)
          case Success(value) => log.debug("Sent event to objet_vinculo " + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }
    }
  }

}