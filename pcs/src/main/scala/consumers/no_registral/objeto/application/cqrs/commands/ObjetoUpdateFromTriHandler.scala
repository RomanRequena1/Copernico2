package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.{ActorRef, ActorSystem}
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import akka.persistence.SnapshotSelectionCriteria
import com.fasterxml.jackson.annotation.JsonIgnore
import consumers.no_registral.objeto.application.cqrs.commands.test.persistSnapshotEvent
import consumers.no_registral.objeto.application.cqrs.commands.test.actualizarObjeto
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoTipo.DmnObjeto
import consumers.no_registral.objeto.application.entities.{ObjetoCommands, ObjetoExternalDto}
import consumers.no_registral.objeto.application.entities.ObjetoCommands.ObjetoUpdateFromTri
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ListDetallesObjeto
import consumers.no_registral.objeto.application.helper.{SendObjetoToObjetoVinculo, testIfObjVinculo}
import consumers.no_registral.objeto.domain.ObjetoEvents
import consumers.no_registral.objeto.domain.ObjetoEvents.ObjetoUpdatedFromTri
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.sujeto.application.entity.SujetoCommands
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import ddd.eventCounterMax
import design_principles.actor_model.Response
import design_principles.actor_model.mechanism.DeliveryIdManagement._
import org.slf4j.{Logger, LoggerFactory}

import scala.util.{Failure, Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor,  requeriment: MonitoringAndMessageProducer) extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {



  /**
   * Si el objeto es tipo M y actor.state.tiene30Objeto es false, entonces informParentTreintaPorciento y si actor.state.tiene30Objeto es true, entonces informParent.
   * En ambos casos, persistSnapshot.
   * En el caso del else, se envía el objeto a objeto vinculo.
   */
  override def handle(
                       command: ObjetoCommands.ObjetoUpdateFromTri
                     ): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    val log: Logger = LoggerFactory.getLogger(this.getClass)

    val semaforo_marca: Option[ListDetallesObjeto] => Option[String] = {
      case Some(d) => d.SOJ_DETALLES.head.SOJ_SEMAFORO_MARCA
      case None => None
    }
    val semaforo_color = command.registro.SOJ_OTROS_ATRIBUTOS.get.SOJ_DETALLES.head.SOJ_SEMAFORO_COLOR

    def isTipo(cmd: ObjetoUpdateFromTri) = {

      val result = DMNTreintaPorcientoTipo.calcularDmn(DmnObjeto(cmd.registro.SOJ_TIPO_OBJETO,
        cmd.registro.SOJ_ADHERIDO_DEBITO.getOrElse(""),
        cmd.registro.SOJ_ESTADO.getOrElse(""),
        cmd.registro.SOJ_TITULARIDAD.getOrElse(""),
        semaforo_color.getOrElse(""),
        "",
        ""
      ))

      result match {
        case f if f.equals(2) =>
          ("2", 2)
        case f if f.equals(-1) =>
          ("1", -1)

        case f if f.equals(1) =>
          ("1", 1)

      }
      //      DMNTreintaPorcientoTipo.dmn(cmd) match {
      //        case f if f.equals(2) =>
      //          ("2", 2)
      //        case f if f.equals(-1) =>
      //          ("1", -1)
      //
      //        case f if f.equals(1) =>
      //          ("1", 1)
      //
      //      }
    }

    val dmn = isTipo(command)
    //    def nuevoRegistro()  = {
    //      if (actor.state.registro.isDefined) {
    //        actualizarObjeto(command.registro.asInstanceOf[ObjetoExternalDto.ObjetosTri], actor.state.registro.asInstanceOf[ObjetoExternalDto.ObjetosTri])
    //      } else {
    //        command.registro
    //      }
    //    }


    var objetoFinal = command.registro

    //    val objetoFinalFinal = objetoFinal match {
    //      case o: ObjetoExternalDto.ObjetosTri if (o.SOJ_SUBTIPO.isDefined && actor.state.registro.isDefined) => o.copy(SOJ_SUBTIPO = objetoFinal.SOJ_SUBTIPO)
    //      case o: ObjetoExternalDto.ObjetosTri if (o.SOJ_SUBTIPO.isEmpty && actor.state.registro.isDefined) => o.copy(SOJ_SUBTIPO = actor.state.registro.get.SOJ_SUBTIPO)
    //      case o: ObjetoExternalDto.ObjetosTri if (o.SOJ_ADHERIDO_DEBITO.isDefined && actor.state.registro.isDefined) => o.copy(SOJ_ADHERIDO_DEBITO = objetoFinal.SOJ_ADHERIDO_DEBITO)
    //      case o: ObjetoExternalDto.ObjetosTri if (o.SOJ_ADHERIDO_DEBITO.isEmpty && actor.state.registro.isDefined) => o.copy(SOJ_ADHERIDO_DEBITO = actor.state.registro.get.SOJ_ADHERIDO_DEBITO)
    //    }

    def filtrarNulls(estado: ObjetoExternalDto.ObjetosTri, eventoA: ObjetoExternalDto) = {
      val evento = eventoA.asInstanceOf[ObjetoExternalDto.ObjetosTri]
      var objetoFinal = evento
      if (evento.SOJ_CAT_SOJ_ID.isDefined && !evento.SOJ_CAT_SOJ_ID.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_CAT_SOJ_ID = evento.SOJ_CAT_SOJ_ID)
      } else if (evento.SOJ_CAT_SOJ_ID.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_CAT_SOJ_ID = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_CAT_SOJ_ID = estado.SOJ_CAT_SOJ_ID)
      }

      if (evento.SOJ_DESCRIPCION.isDefined && !evento.SOJ_DESCRIPCION.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_DESCRIPCION = evento.SOJ_DESCRIPCION)
      } else if (evento.SOJ_DESCRIPCION.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_DESCRIPCION = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_DESCRIPCION = estado.SOJ_DESCRIPCION)
      }

      if (evento.SOJ_ESTADO.isDefined && !evento.SOJ_ESTADO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_ESTADO = evento.SOJ_ESTADO)
      } else if (evento.SOJ_ESTADO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_ESTADO = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_ESTADO = estado.SOJ_ESTADO)
      }

      if (evento.SOJ_FECHA_INICIO.isDefined && !evento.SOJ_FECHA_INICIO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_FECHA_INICIO = evento.SOJ_FECHA_INICIO)
      } else if (evento.SOJ_FECHA_INICIO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_FECHA_INICIO = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_FECHA_INICIO = estado.SOJ_FECHA_INICIO)
      }

      if (evento.SOJ_FECHA_FIN.isDefined && !evento.SOJ_FECHA_FIN.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_FECHA_FIN = evento.SOJ_FECHA_FIN)
      } else if (evento.SOJ_FECHA_FIN.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_FECHA_FIN = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_FECHA_FIN = estado.SOJ_FECHA_FIN)
      }

      if (evento.SOJ_ID_EXTERNO.isDefined && !evento.SOJ_ID_EXTERNO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_ID_EXTERNO = evento.SOJ_ID_EXTERNO)
      } else if (evento.SOJ_ID_EXTERNO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_ID_EXTERNO = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_ID_EXTERNO = estado.SOJ_ID_EXTERNO)
      }

      if (evento.SOJ_OTROS_ATRIBUTOS.isDefined && !evento.SOJ_OTROS_ATRIBUTOS.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_OTROS_ATRIBUTOS = evento.SOJ_OTROS_ATRIBUTOS)
      } else if (evento.SOJ_OTROS_ATRIBUTOS.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_OTROS_ATRIBUTOS = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_OTROS_ATRIBUTOS = estado.SOJ_OTROS_ATRIBUTOS)
      }

      if (evento.SOJ_BASE_IMPONIBLE.isDefined && !evento.SOJ_BASE_IMPONIBLE.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_BASE_IMPONIBLE = evento.SOJ_BASE_IMPONIBLE)
      } else if (evento.SOJ_BASE_IMPONIBLE.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_BASE_IMPONIBLE = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_BASE_IMPONIBLE = estado.SOJ_BASE_IMPONIBLE)
      }

      if (evento.SOJ_ADHERIDO_DEBITO.isDefined && !evento.SOJ_ADHERIDO_DEBITO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = evento.SOJ_ADHERIDO_DEBITO)
      } else if (evento.SOJ_ADHERIDO_DEBITO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = estado.SOJ_ADHERIDO_DEBITO)
      }

      if (evento.SOJ_CANT_CUOTAS_PAGADAS.isDefined && !evento.SOJ_CANT_CUOTAS_PAGADAS.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_CANT_CUOTAS_PAGADAS = evento.SOJ_CANT_CUOTAS_PAGADAS)
      } else if (evento.SOJ_CANT_CUOTAS_PAGADAS.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_CANT_CUOTAS_PAGADAS = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_CANT_CUOTAS_PAGADAS = estado.SOJ_CANT_CUOTAS_PAGADAS)
      }

      if (evento.SOJ_CANAL_ORIGEN.isDefined && !evento.SOJ_CANAL_ORIGEN.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_CANAL_ORIGEN = evento.SOJ_CANAL_ORIGEN)
      } else if (evento.SOJ_CANAL_ORIGEN.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_CANAL_ORIGEN = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_CANAL_ORIGEN = estado.SOJ_CANAL_ORIGEN)
      }

      if (evento.SOJ_SUBTIPO.isDefined && !evento.SOJ_SUBTIPO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = evento.SOJ_SUBTIPO)
      } else if (evento.SOJ_SUBTIPO.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = estado.SOJ_SUBTIPO)
      }

      if (evento.SOJ_IDENTIFICADOR_2.isDefined && !evento.SOJ_IDENTIFICADOR_2.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_IDENTIFICADOR_2 = evento.SOJ_IDENTIFICADOR_2)
      } else if (evento.SOJ_IDENTIFICADOR_2.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_IDENTIFICADOR_2 = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_IDENTIFICADOR_2 = estado.SOJ_IDENTIFICADOR_2)
      }

      if (evento.SOJ_TITULARIDAD.isDefined && !evento.SOJ_TITULARIDAD.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_TITULARIDAD = evento.SOJ_TITULARIDAD)
      } else if (evento.SOJ_TITULARIDAD.contains("null")) {
        objetoFinal = objetoFinal.copy(SOJ_TITULARIDAD = None)
      } else {
        objetoFinal = objetoFinal.copy(SOJ_TITULARIDAD = estado.SOJ_TITULARIDAD)
      }

      objetoFinal
    }

    val objetoFFF: ObjetoExternalDto = actor.state.registro match {
      case Some(registro) => registro match {
        case o: ObjetoExternalDto.ObjetosTri => filtrarNulls(o, command.registro)
        case _ => command.registro
      }
      case None => command.registro
    }

    //    if (command.registro.SOJ_SUBTIPO.isDefined) {
    //      objetoFinal match {
    //        case o: ObjetoExternalDto.ObjetosTri =>
    //      }
    //      objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = evento.SOJ_SUBTIPO)
    //    } else {
    //      objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = estado.SOJ_SUBTIPO)
    //    }
    //
    //    if (command.registro.SOJ_ADHERIDO_DEBITO.isDefined) {
    //      objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = evento.SOJ_ADHERIDO_DEBITO)
    //    } else {
    //      objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = estado.SOJ_ADHERIDO_DEBITO)
    //    }


    val event = ObjetoEvents.ObjetoUpdatedFromTri(
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      objetoFFF,
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito,
      Some(dmn._1),
      Some(dmn._2)
    )


    if(isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents)
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)

      Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    } else {
      persistSnapshotEvent(event, actor, command, requeriment)
    }
  }
}


object test {
  def actualizarObjeto(evento: ObjetoExternalDto.ObjetosTri, estado: ObjetoExternalDto.ObjetosTri) = {
    /*RULE_NUMBER: Option[String],
    EV_ID: BigInt,
    SOJ_SUJ_IDENTIFICADOR: String,
    SOJ_TIPO_OBJETO: String,
    SOJ_IDENTIFICADOR: String,
    SOJ_CAT_SOJ_ID: Option[String],
    SOJ_DESCRIPCION: Option[String],
    SOJ_ESTADO: Option[String],
    SOJ_FECHA_INICIO: Option[LocalDateTime],
    SOJ_FECHA_FIN: Option[LocalDateTime],
    SOJ_ID_EXTERNO: Option[String],
    SOJ_OTROS_ATRIBUTOS: Option[ListDetallesObjeto],
    SOJ_BASE_IMPONIBLE: Option[BigDecimal],
    SOJ_ADHERIDO_DEBITO: Option[String],
    SOJ_CANT_CUOTAS_PAGADAS: Option[BigInt],
    SOJ_CANAL_ORIGEN: Option[String],
    SOJ_SUBTIPO: Option[String], VACIO
    SOJ_IDENTIFICADOR_2: Option[String],
    SOJ_TITULARIDAD: Option[String]*/


    // estado: SOJ_SUBTIPO: "TIPO uno"
    // null
    var objetoFinal = evento

    if (evento.SOJ_SUBTIPO.isDefined) {
      objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = evento.SOJ_SUBTIPO)
    } else {
      objetoFinal = objetoFinal.copy(SOJ_SUBTIPO = estado.SOJ_SUBTIPO)
    }

    if (evento.SOJ_ADHERIDO_DEBITO.isDefined) {
      objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = evento.SOJ_ADHERIDO_DEBITO)
    } else {
      objetoFinal = objetoFinal.copy(SOJ_ADHERIDO_DEBITO = estado.SOJ_ADHERIDO_DEBITO)
    }

    objetoFinal
  }

  def persistSnapshotEvent(event: ObjetoUpdatedFromTri, actor: ObjetoActor, command: ObjetoUpdateFromTri,requeriment: MonitoringAndMessageProducer ): Success[Response.SuccessProcessing] = {
    val sender = actor.context.sender()
    @JsonIgnore
    val log: Logger = LoggerFactory.getLogger(this.getClass)



    implicit val ac: ActorSystem = actor.context.system
    val Obje: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    actor.persistEvent(event) { () =>
      val a = actor.state.registro
      val evento = event.registro

      //      evento match {
      //        case ObjetoExternalDto.ObjetosTri(RULE_NUMBER, EV_ID, SOJ_SUJ_IDENTIFICADOR, SOJ_TIPO_OBJETO,
      //          SOJ_IDENTIFICADOR, SOJ_CAT_SOJ_ID, SOJ_DESCRIPCION, SOJ_ESTADO, SOJ_FECHA_INICIO, SOJ_FECHA_FIN,
      //          SOJ_ID_EXTERNO, SOJ_OTROS_ATRIBUTOS, SOJ_BASE_IMPONIBLE, SOJ_ADHERIDO_DEBITO, SOJ_CANT_CUOTAS_PAGADAS, SOJ_CANAL_ORIGEN,
      //          SOJ_SUBTIPO, SOJ_IDENTIFICADOR_2, SOJ_TITULARIDAD) => ???
      //        case o: ObjetoExternalDto.ObjetosTri => o.copy()
      //      }


      /*val miNuevoRegistro = actor.state.registro match {
        case Some(estado) => estado match {
          case objetoEstado: ObjetoExternalDto.ObjetosTri => {
            evento match {
              case x: ObjetoExternalDto.ObjetosTri =>
                  var nuevoObjeto: ObjetoExternalDto.ObjetosTri = ObjetoExternalDto.ObjetosTri(RULE_NUMBER = (objetoEstado || x.RULE_NUMBER))
//                x.RULE_NUMBER
//                x.EV_ID
//                x.SOJ_SUJ_IDENTIFICADOR
//                x.SOJ_TIPO_OBJETO
//                x.SOJ_IDENTIFICADOR
//                x.SOJ_OTROS_ATRIBUTOS
                if (x.SOJ_CAT_SOJ_ID.isDefined) {
              a
                }
                x.SOJ_DESCRIPCION
                x.SOJ_ESTADO
                x.SOJ_FECHA_INICIO
                x.SOJ_FECHA_FIN
                x.SOJ_ID_EXTERNO
                x.SOJ_BASE_IMPONIBLE
                x.SOJ_ADHERIDO_DEBITO
                x.SOJ_CANT_CUOTAS_PAGADAS
                x.SOJ_CANAL_ORIGEN
                x.SOJ_SUBTIPO
                x.SOJ_IDENTIFICADOR_2
                x.SOJ_TITULARIDAD
                x

                val dddd = x.productIterator.collect {
                  case field: Option[_] if field.exists(_.toString.nonEmpty) => field
                  // Recoge solo los campos Option que no son vacíos
                }.toList

            }
            objetoEstado.copy(SOJ_DESCRIPCION = evento.SOJ_DESCRIPCION)
          }
        }
        case None => "El registro del evento"
      }*/



      //      val olapa: List[Option[_]] = evento match {
      //        case miObjeto: ObjetoExternalDto.ObjetosTri =>
      //          val dddd = miObjeto.productIterator.collect {
      //            case field: Option[_] if field.exists(_.toString.nonEmpty) => field
      //            // Recoge solo los campos Option que no son vacíos
      //          }.toList
      //          dddd
      //        case _ => null
      //      }
      actor.state += event
      //todo juicio persiste, pero no se us apara el calculo del 30%?

      if (actor.state.registro.get.SOJ_TIPO_OBJETO.equals("M")) { // todo tipo M , pero si para el calculo de deuda para un sujeto. Objeto juicio queda atado a cuit, pero no se va a teber en cuanta cuando se calcule el 30%, no se guarda el vinculo.
        if (actor.state.tiene30Objeto.equals(false)) {
          val res = actor.context.parent.ask[Response.SuccessProcessing](SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento(
            command.deliveryId,
            command.sujetoId,
            command.objetoId,
            command.tipoObjeto,
            actor.state.saldo,
            actor.state.obligacionesSaldo.values.sum,
            actor.state.clasificacionObjeto
          ))
          res.onComplete {
            case Failure(exception) => log.error("Error to send event to sujeto tipo M false " + exception + " objID: "+ command.objetoId + "sujID: "+ command.sujetoId)
            case Success(value) => log.debug("Sent event to sujeto tipo M false " + " objID: "+ command.objetoId + " sujID: "+ command.sujetoId)
          }
        } else {
          {
            val res = actor.context.parent.ask[Response.SuccessProcessing](SujetoCommands.SujetoUpdateFromObjeto(
              command.deliveryId,
              command.sujetoId,
              command.objetoId,
              command.tipoObjeto,
              actor.state.saldo,
              actor.state.obligacionesSaldo.values.sum,
              actor.state.clasificacionObjeto
            ))
            res.onComplete {
              case Failure(exception) => log.error("Error to send event to sujeto tipo M true " + exception + " objID: "+ command.objetoId + "sujID: "+ command.sujetoId)
              case Success(value) => log.debug("Sent event to sujeto tipo M true " + " objID: "+ command.objetoId + " sujID: "+ command.sujetoId)
            }
          }
        }
        actor.persistSnapshot(event, actor.state) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      }
      else {
        SendObjetoToObjetoVinculo(Obje,actor, command.sujetoId, command.objetoId, command.tipoObjeto, command.registro.SOJ_ESTADO, requeriment)
      }
      //actor.informParent(command, actor.state) //todo saque el infoparent, deberia hacer el nuevo handler
      if (actor.state.eventCounter == eventCounterMax) {
        actor.deleteSnapshots(SnapshotSelectionCriteria(actor.lastSequenceNr - 2))
        actor.saveSnapshot(actor.state.copy(eventCounter = 0))
      }
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
    }
    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }
}