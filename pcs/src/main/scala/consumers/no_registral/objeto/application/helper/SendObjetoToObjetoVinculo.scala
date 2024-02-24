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
import org.slf4j.{Logger, LoggerFactory}
import consumers.no_registral.objeto.application.helper.testAnda
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
  protected val log: Logger = LoggerFactory.getLogger(this.getClass)

  def apply(actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer): Unit = {

    val objetoVinculoMessageRoots = ObjetoVinculoMessageRoots(objetoId).toString
    //implicit val system: ActorSystem = actor.context.system


    val actorPath = s"akka://PersonClassificationService/system/sharding/SujetoActor/*/${sujetoId}/Sujeto-${sujetoId}-Objeto-${objetoId}-${tipoObjeto}/ObjetoVinculo-${objetoId}"
    // s"akka://PersonClassificationService/user/ObjetoVinculo-${objetoId}"
    log.error("Se va a crear  en none del actor" + sujetoId + " - " + objetoId + ")")
    testAnda(actor, sujetoId, objetoId, tipoObjeto, estado, requeriment)

//    actor.context.child(objetoVinculoMessageRoots) match {
//      case Some(value) => {
//
//        estado match {
//          case x if x.getOrElse("").equals("TRANSF") =>
//            log.error("TAPA creado CREO TRANSF VINCULOOBJETO" + sujetoId)
//            value ! CreateTransfVinculoObjetoFromObj(objetoId = objetoId,
//              sujetoId = sujetoId,
//              deliveryId = 0,
//              tipoObj = tipoObjeto,
//              tiene30Objeto = actor.state.tiene30Objeto,
//              isResponsable = Some(actor.state.isResponsable),
//              estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
//              titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
//            if (actor.state.tiene30Objeto.equals(false))
//              actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
//            else
//              actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
//
//          case x if x.getOrElse("").equals("BAJA") =>
//            log.error("TAPA creado ELIMINO VINCOBJETO" + sujetoId)
//            value ! RemoveObjetoVinculo(objetoId = objetoId,
//              sujetoId = sujetoId,
//              deliveryId = 0,
//              tipoObj = tipoObjeto,
//              tiene30Objeto = actor.state.tiene30Objeto,
//              isResponsable = Some(actor.state.isResponsable),
//              estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
//              titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
//          case _ =>
//            log.error("TAPA creado CREO UPDATEVINCULOOBJETO" + sujetoId)
//            value ! UpdateVinculoObjetoFromObj(objetoId = objetoId,
//              sujetoId = sujetoId,
//              deliveryId = 0,
//              tipoObj = tipoObjeto,
//              tiene30Objeto = actor.state.tiene30Objeto,
//              isResponsable = Some(actor.state.isResponsable),
//              estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
//              titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD)
//        }
////        log.error("TAPA Se cumplio el created  en none del actor" + sujetoId + " - " + objetoId + ")")
////        //Future(actor.context.stop(value)).isCompleted //todo probar despues
////        for {
////          _ <- Future(actor.context.stop(value))
////          _ <- Future(testAnda(actor1, sujetoId, objetoId, tipoObjeto, estado, requeriment))
////        } yield {log.error("Se cumplio el stop del actor"+ sujetoId + " - " + objetoId + ")")}
//      }
//      case None => {
//        log.error("TAPA Se va a crear  en none del actor" + sujetoId + " - " + objetoId + ")")
//        testAnda(actor, sujetoId, objetoId, tipoObjeto, estado, requeriment)
//      }
//    }
  }
}