package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.{ObjetoRemoveObligacion, ObjetoUpdateFromObligacion, ObjetoUpdateFromObnTreintaPorciento, ObjetoUpdateFromTri}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.{CreateTransfVinculoObjetoFromObj, UpdateVinculoObjetoFromObj}
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage.ObjetoVinculoMessageRoots
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.actor_model.Response


//todo --------------------------- REFACTORIZAR ----------------------------------
object SendObjetoToObjetoVinculo {
  val obj_default: ObjetosTri = ObjetosTri(Some("None"),0,"None","None","None",Some("None"),Some("None"),Some("None"),None,None,Some("None"),None,Some(0),Some("None"),Some(0),Some("None"),Some("None"),Some("None"),Some("None"))
  def apply(actor: ObjetoActor,sujetoId: String, objetoId: String, tipoObjeto: String,estado: Option[String], requeriment: MonitoringAndMessageProducer ): Unit = {
    println("CUMBIA SendObjetoToObjetoVinculo  -> ")
    val tranferenciaMessageRoots = ObjetoVinculoMessageRoots(objetoId).toString
    implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    try {
      implicit val actorTranf: ActorRef = system.actorOf(actorProp, tranferenciaMessageRoots)

      if(estado.getOrElse("").equals("TRANSF")) {
        actorTranf ! CreateTransfVinculoObjetoFromObj(0,
          sujetoId,
          objetoId,
          tipoObjeto,
          actor.state.tiene30Objeto,
          Some(actor.state.isResponsable),
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
        )
        if(actor.state.tiene30Objeto.equals(false))
          actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents,sujetoId,objetoId, tipoObjeto, actor.state)
        else
          actor.informParent(actor.state.lastDeliveryIdByEvents,sujetoId,objetoId, tipoObjeto, actor.state)

      } else
      actorTranf ! UpdateVinculoObjetoFromObj(0,
        sujetoId,
        objetoId,
        tipoObjeto,
        actor.state.tiene30Objeto,
        Some(actor.state.isResponsable),
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
        actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
      )
    }
    catch {
      case e:Exception => {
        val act = actor.context.actorSelection(s"akka://PersonClassificationService/user/ObjetoVinculo-${objetoId}")
        if(estado.getOrElse("").equals("TRANSF")) {
          act ! CreateTransfVinculoObjetoFromObj(0,
            sujetoId,
            objetoId,
            tipoObjeto,
            actor.state.tiene30Objeto,
            Some(actor.state.isResponsable),
            actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
            actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD
          )
          if(actor.state.tiene30Objeto.equals(false))
            actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents,sujetoId,objetoId, tipoObjeto, actor.state)
          else
            actor.informParent(actor.state.lastDeliveryIdByEvents,sujetoId,objetoId, tipoObjeto, actor.state)
        }
        else
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
