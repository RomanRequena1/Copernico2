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


//todo --------------------------- REFACTORIZAR ----------------------------------
object SendObjetoToObjetoVinculo {
  val obj_default: ObjetosTri = ObjetosTri(Some("None"),0,"None","None","None",Some("None"),Some("None"),Some("None"),None,None,Some("None"),None,Some(0),Some("None"),Some(0),Some("None"),Some("None"),Some("None"),Some("None"))
  /**
   * 1. Crear actor
   * Si el actor ya existe, se envía el mensaje al actor existente que entra por el catch y si no existe se crea el actor
   * Si estado es TRANSF se envia el mensaje CreateTransfVinculoObjetoFromObj y se hace los informes hacia el padre
   * Si estado no es TRANSF se envia el mensaje UpdateVinculoObjetoFromObj
   */
  def apply(actor: ObjetoActor,sujetoId: String, objetoId: String, tipoObjeto: String,estado: Option[String], requeriment: MonitoringAndMessageProducer ): Unit = {
    println("CUMBIA SendObjetoToObjetoVinculo  -> ")
    val objetoVinculoMessageRoots = ObjetoVinculoMessageRoots(objetoId).toString
    implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    try {
      implicit val actorObjetoVinculo: ActorRef = system.actorOf(actorProp, objetoVinculoMessageRoots)
      estado match {
        case x if x.getOrElse("").equals("TRANSF") =>
          println("CREO VINCULOOBJETO"+sujetoId)
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
          println("ELIMINO VINCOBJETO"+sujetoId)
          println("llego al if baja")
          actorObjetoVinculo ! RemoveObjetoVinculo(0,
            sujetoId,
            objetoId,
            tipoObjeto,
            actor.state.tiene30Objeto,
            Some(actor.state.isResponsable),
            actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
            actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
        case _ =>
          println("CREO UPDATEVINCULOOBJETO"+sujetoId)
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
    }
    catch {
      case e: Exception => {
        val act = actor.context.actorSelection(s"akka://PersonClassificationService/user/ObjetoVinculo-${objetoId}")
        estado match {
          case x if x.getOrElse("").equals("TRANSF") =>
            println("CREO VINCULOOBJETO"+sujetoId)
            act ! CreateTransfVinculoObjetoFromObj(0,
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
            println("llego al if baja")
            act ! RemoveObjetoVinculo(0,
              sujetoId,
              objetoId,
              tipoObjeto,
              actor.state.tiene30Objeto,
              Some(actor.state.isResponsable),
              actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
              actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
          case _ =>
            println("CREO UPDATEVINCULOOBJETO"+sujetoId)
            act ! UpdateVinculoObjetoFromObj(0,
              sujetoId,
              objetoId,
              tipoObjeto,
              actor.state.tiene30Objeto,
              Some(actor.state.isResponsable),
              actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
              actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
            )
        }
      }
    }
  }
}
