package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands.{ObjetoRemoveObligacion, ObjetoUpdateFromObligacion, ObjetoUpdateFromObnTreintaPorciento, ObjetoUpdateFromTri}
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.UpdateVinculoObjetoFromObj
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoMessage.ObjetoVinculoMessageRoots
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor

object SendObjetoToObjetoVinculo {
  val obj_default: ObjetosTri = ObjetosTri(Some("None"),0,"None","None","None",Some("None"),Some("None"),Some("None"),None,None,Some("None"),None,Some(0),Some("None"),Some(0),Some("None"),Some("None"),Some("None"),Some("None"))
  def apply(actor: ObjetoActor, command: ObjetoUpdateFromTri, requeriment: MonitoringAndMessageProducer ): Unit = {
    println("CUMBIA SendObjetoToObjetoVinculo ObjetoUpdateFromTri -> ")
    val tranferenciaMessageRoots = ObjetoVinculoMessageRoots(command.objetoId).toString
    implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    try {
      implicit val actorTranf: ActorRef = system.actorOf(actorProp, tranferenciaMessageRoots)

      actorTranf ! UpdateVinculoObjetoFromObj(0,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        actor.state.tiene30Objeto,
        Some(actor.state.isResponsable),
        command.registro.SOJ_ESTADO,
        command.registro.SOJ_TITULARIDAD
      )
    }
    catch {
      case e:Exception => {
        val act = actor.context.actorSelection(s"akka://PersonClassificationService/user/ObjetoVinculo-${command.objetoId}")
        act ! UpdateVinculoObjetoFromObj(0,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          actor.state.tiene30Objeto,
          Some(actor.state.isResponsable),
          command.registro.SOJ_ESTADO,
          command.registro.SOJ_TITULARIDAD)
      }
    }
  }

  def apply(actor: ObjetoActor, command: ObjetoUpdateFromObligacion, requeriment: MonitoringAndMessageProducer ): Unit = {
    println("CUMBIA SendObjetoToObjetoVinculo ObjetoUpdateFromObligacion -> ")
    val tranferenciaMessageRoots = ObjetoVinculoMessageRoots(command.objetoId).toString
    implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    try {
      implicit val actorTranf: ActorRef = system.actorOf(actorProp, tranferenciaMessageRoots)

      actorTranf ! UpdateVinculoObjetoFromObj(0,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        actor.state.tiene30Objeto,
        Some(actor.state.isResponsable),
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO
      )
    }
    catch {
      case e:Exception => {
        val act = actor.context.actorSelection(s"akka://PersonClassificationService/user/ObjetoVinculo-${command.objetoId}")
        act ! UpdateVinculoObjetoFromObj(0,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          actor.state.tiene30Objeto,
          Some(actor.state.isResponsable),
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO
        )
      }
    }
  }

  def apply(actor: ObjetoActor, command: ObjetoRemoveObligacion, requeriment: MonitoringAndMessageProducer ): Unit = {
    println("CUMBIA SendObjetoToObjetoVinculo ObjetoRemoveObligacion -> ")
    val tranferenciaMessageRoots = ObjetoVinculoMessageRoots(command.objetoId).toString
    implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    try {
      implicit val actorTranf: ActorRef = system.actorOf(actorProp, tranferenciaMessageRoots)

      actorTranf ! UpdateVinculoObjetoFromObj(0,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        actor.state.tiene30Objeto,
        Some(actor.state.isResponsable),
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO
      )
    }
    catch {
      case e:Exception => {
        val act = actor.context.actorSelection(s"akka://PersonClassificationService/user/ObjetoVinculo-${command.objetoId}")
        act ! UpdateVinculoObjetoFromObj(0,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          actor.state.tiene30Objeto,
          Some(actor.state.isResponsable),
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO
        )
      }
    }
  }

  def apply(actor: ObjetoActor, command: ObjetoUpdateFromObnTreintaPorciento, requeriment: MonitoringAndMessageProducer ): Unit = {
    println("CUMBIA SendObjetoToObjetoVinculo ObjetoUpdateFromObnTreintaPorciento -> ")
    val tranferenciaMessageRoots = ObjetoVinculoMessageRoots(command.objetoId).toString
    implicit val system: ActorSystem = actor.context.system
    implicit val actorProp: Props = ObjetoVinculoActor.props(requeriment)


    try {
      implicit val actorTranf: ActorRef = system.actorOf(actorProp, tranferenciaMessageRoots)

      actorTranf ! UpdateVinculoObjetoFromObj(0,
        command.sujetoId,
        command.objetoId,
        command.tipoObjeto,
        actor.state.tiene30Objeto,
        Some(actor.state.isResponsable),
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
        actor.state.registro.getOrElse(obj_default).SOJ_ESTADO
      )
    }
    catch {
      case e:Exception => {
        val act = actor.context.actorSelection(s"akka://PersonClassificationService/user/ObjetoVinculo-${command.objetoId}")
        act ! UpdateVinculoObjetoFromObj(0,
          command.sujetoId,
          command.objetoId,
          command.tipoObjeto,
          actor.state.tiene30Objeto,
          Some(actor.state.isResponsable),
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          actor.state.registro.getOrElse(obj_default).SOJ_ESTADO
        )
      }
    }
  }




}
