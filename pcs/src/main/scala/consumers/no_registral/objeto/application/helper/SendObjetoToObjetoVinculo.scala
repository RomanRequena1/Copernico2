package consumers.no_registral.objeto.application.helper

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands.{CreateTransfVinculoObjetoFromObj, RemoveObjetoVinculo, UpdateVinculoObjetoFromObj}
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import design_principles.actor_model.{Command, Response}
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success}

object SendObjetoToObjetoVinculo {
  val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"),Some("None"), Some("None"), Some("None"),None,None)

  def apply(vinculoActor: ActorRef, actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer, command: Command): Unit = {
    println(s"[SEND-OBJ-VINCULO-APPLY] deliveryId=${command.deliveryId}, sujetoId=$sujetoId, objetoId=$objetoId, " +
      s"tipoObjeto=$tipoObjeto, estado=$estado, vinculoActorPath=${vinculoActor.path}")

    if(vinculoActor.path.toString.equals("akka://PersonClassificationService/system/sharding/ObjetoVinculoActor")){
      println(s"[SEND-OBJ-VINCULO-APPLY] Usando vinculoActor existente")
      testIfObjVinculo(vinculoActor, actor, sujetoId, objetoId, tipoObjeto, estado, requeriment, command)
    } else {
      println(s"[SEND-OBJ-VINCULO-APPLY] Creando nuevo vinculoActor")
      implicit val ac: ActorSystem = actor.context.system
      val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
      testIfObjVinculo(vinculoActor, actor, sujetoId, objetoId, tipoObjeto, estado, requeriment, command)
    }
  }
}

object testIfObjVinculo {

  def apply(vinculoActor: ActorRef, actor: ObjetoActor, sujetoId: String, objetoId: String, tipoObjeto: String, estado: Option[String], requeriment: MonitoringAndMessageProducer, command: Command): Unit = {
    val obj_default: ObjetosTri = ObjetosTri(Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"), None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"), Some("None"), Some("None"),Some("None"), Some("None"), Some("None"),None,None)

    val log: Logger = LoggerFactory.getLogger(this.getClass)
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global

    println(s"[TEST-IF-OBJ-VINCULO-ENTRY] deliveryId=${command.deliveryId}, " +
      s"sujetoId=$sujetoId, objetoId=$objetoId, tipoObjeto=$tipoObjeto, " +
      s"estado=$estado, commandType=${command.getClass.getSimpleName}, " +
      s"tiene30Objeto=${actor.state.tiene30Objeto}")

    val esPago = command match {
      case _: ObjetoCommands.ObjetoRemoveObligacion => true
      case _ => false
    }

    println(s"[TEST-IF-OBJ-VINCULO-EVAL] deliveryId=${command.deliveryId}, objetoId=$objetoId, " +
      s"esPago=$esPago, estadoValue=${estado.getOrElse("NONE")}")

    estado match {
      case x if (x.getOrElse("").equals("TRANSF") || x.getOrElse("").equals("ESTADO2")) && !esPago =>
        println(s"[TEST-IF-OBJ-VINCULO-CASE-TRANSF] deliveryId=${command.deliveryId}, objetoId=$objetoId - " +
          s"Enviando CreateTransfVinculoObjetoFromObj")

        val res = vinculoActor.ask[Response.SuccessProcessing](CreateTransfVinculoObjetoFromObj(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = actor.state.lastDeliveryIdByEvents,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto,
          idExterno = actor.state.idExterno,
          dmnNumero = actor.state.dmnNumero,
          dmnDescripcion = actor.state.dmnDescripcion
        ))
        res.onComplete {
          case Failure(exception) =>
            println(s"[TEST-IF-OBJ-VINCULO-TRANSF-FAIL] deliveryId=${command.deliveryId}, objetoId=$objetoId - ERROR: ${exception.getMessage}")
            log.error("Error to send event to objeto_vinculo (TRANSF)" + exception + "objID: "+ objetoId + "sujID: "+sujetoId)
          case Success(value) =>
            println(s"[TEST-IF-OBJ-VINCULO-TRANSF-OK] deliveryId=${command.deliveryId}, objetoId=$objetoId - SUCCESS")
            log.debug("Sent event to objet_vinculo (TRANSF)" + " objID: "+ objetoId + " sujID: "+ sujetoId)
        }

        if (actor.state.tiene30Objeto.equals(false))
          actor.informParentTreintaPorciento(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)
        else
          actor.informParent(actor.state.lastDeliveryIdByEvents, sujetoId, objetoId, tipoObjeto, actor.state)

      case x if (x.getOrElse("").equals("TRANSF") || x.getOrElse("").equals("ESTADO2")) && esPago =>
        println(s"[TEST-IF-OBJ-VINCULO-CASE-PAGO-TRANSF] deliveryId=${command.deliveryId}, objetoId=$objetoId - " +
          s"Enviando UpdateVinculoObjetoFromObj (pago en transferido)")

        val res = vinculoActor.ask[Response.SuccessProcessing](UpdateVinculoObjetoFromObj(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = actor.state.lastDeliveryIdByEvents,
          idExterno = actor.state.idExterno,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto,
          dmnNumero = actor.state.dmnNumero,
          dmnDescripcion = actor.state.dmnDescripcion
        ))
        res.onComplete {
          case Failure(exception) => {
            println(s"[TEST-IF-OBJ-VINCULO-PAGO-TRANSF-FAIL] deliveryId=${command.deliveryId}, objetoId=$objetoId - ERROR: ${exception.getMessage}")
            requeriment.monitoring.counter("objeto-vinculo-pago-transf-error").increment()
            log.error("Error to send event to objeto_vinculo (PAGO-TRANSF) " + exception + " objID: " + objetoId + " sujID: " + sujetoId)
          }
          case Success(value) => {
            println(s"[TEST-IF-OBJ-VINCULO-PAGO-TRANSF-OK] deliveryId=${command.deliveryId}, objetoId=$objetoId - SUCCESS")
            requeriment.monitoring.counter("objeto-vinculo-pago-transf-success").increment()
            log.debug("Sent event to objet_vinculo (PAGO-TRANSF)" + " objID: " + objetoId + " sujID: " + sujetoId)
          }
        }

      case x if x.getOrElse("").equals("BAJA") =>
        println(s"[TEST-IF-OBJ-VINCULO-CASE-BAJA] deliveryId=${command.deliveryId}, objetoId=$objetoId - " +
          s"Enviando RemoveObjetoVinculo")

        val res = vinculoActor.ask[Response.SuccessProcessing](RemoveObjetoVinculo(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = actor.state.lastDeliveryIdByEvents,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto))
        res.onComplete {
          case Failure(exception) => {
            println(s"[TEST-IF-OBJ-VINCULO-BAJA-FAIL] deliveryId=${command.deliveryId}, objetoId=$objetoId - ERROR: ${exception.getMessage}")
            requeriment.monitoring.counter("objeto-vinculo-baja-error").increment()
            log.error("Error to send event to objeto_vinculo (BAJA) " + exception + " objID: " + objetoId + "sujID: " + sujetoId)
          }
          case Success(value) => {
            println(s"[TEST-IF-OBJ-VINCULO-BAJA-OK] deliveryId=${command.deliveryId}, objetoId=$objetoId - SUCCESS")
            requeriment.monitoring.counter("objeto-vinculo-baja-success").increment()
            log.debug("Sent event to objet_vinculo (BAJA) " + " objID: " + objetoId + " sujID: " + sujetoId)
          }
        }

      case _ =>
        println(s"[TEST-IF-OBJ-VINCULO-CASE-DEFAULT] deliveryId=${command.deliveryId}, objetoId=$objetoId, " +
          s"estado=$estado - Enviando UpdateVinculoObjetoFromObj via tell (!)")

        vinculoActor ! UpdateVinculoObjetoFromObj(
          objetoId = objetoId,
          sujetoId = sujetoId,
          deliveryId = command.deliveryId,
          idExterno = actor.state.idExterno,
          tipoObj = tipoObjeto,
          tiene30Objeto = actor.state.tiene30Objeto,
          isResponsable = Some(actor.state.isResponsable),
          estadoObj = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO,
          titularidad = actor.state.registro.getOrElse(obj_default).SOJ_TITULARIDAD,
          exclusionObjeto = actor.state.exclusionObjeto,
          dmnNumero = actor.state.dmnNumero,
          dmnDescripcion = actor.state.dmnDescripcion)

        println(s"[TEST-IF-OBJ-VINCULO-CASE-DEFAULT-SENT] deliveryId=${command.deliveryId}, objetoId=$objetoId - " +
          s"Mensaje enviado (fire-and-forget)")
    }
  }
}