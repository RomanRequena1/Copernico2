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
import consumers.no_registral.objeto.application.helper.{testIfObjVinculo, SendObjetoToObjetoVinculo}
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

import java.lang.reflect.Field
import scala.util.{Failure, Success, Try}

class ObjetoUpdateFromTriHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
    extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromTri] {

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

      val result = DMNTreintaPorcientoTipo.calcularDmn(
        DmnObjeto(
          cmd.registro.SOJ_TIPO_OBJETO,
          cmd.registro.SOJ_ADHERIDO_DEBITO.getOrElse(""),
          cmd.registro.SOJ_ESTADO.getOrElse(""),
          cmd.registro.SOJ_TITULARIDAD.getOrElse(""),
          semaforo_color.getOrElse(""),
          "",
          ""
        )
      )

      result match {
        case f if f.equals(2) =>
          ("2", 2)
        case f if f.equals(-1) =>
          ("1", -1)

        case f if f.equals(1) =>
          ("1", 1)

      }
    }

    val dmn = isTipo(command)

    //      val eventoNuevo = declaredFields.map {
    //        case campo if campo.get(cc) == null => campo.set(cc, estado.getClass.getField(campo.toString).get(estado))
    //        case campo if campo.get(cc) == "null" => campo.set(cc, None)
    //      }

//    val declaredFolded = declaredFields.foldLeft(Map.empty[String, Any]) { (a, f) =>
//      f.setAccessible(true)
//      a + (f.getName -> f.get(cc))
//    }

    def getCCParams(eventoA: ObjetoExternalDto, estado: ObjetoExternalDto.ObjetosTri) = {
      val evento = eventoA.asInstanceOf[ObjetoExternalDto.ObjetosTri]
      val declaredFields = evento.getClass.getDeclaredFields
      var objetoNuevoTest = evento

      val test2 = declaredFields.map { campo =>
        val campoEvento = objetoNuevoTest.getClass.getDeclaredField(campo.getName)
        val campoEstado = estado.getClass.getDeclaredField(campo.getName)
        campoEvento.setAccessible(true)
        campoEstado.setAccessible(true)

        if (campoEvento.get(evento) == None) {
          campoEvento.set(objetoNuevoTest, campoEstado.get(estado))
        } else if (campoEvento.get(evento).equals(Some("null"))) {
          campoEvento.set(objetoNuevoTest, None)
        }
      }
      objetoNuevoTest
    }

    def getObjetoFFF() = {
      val objetoFFF: ObjetoExternalDto.ObjetosTri = actor.state.registro match {
        case Some(value) => getCCParams(command.registro, actor.state.registro.get.asInstanceOf[ObjetoExternalDto.ObjetosTri])
        case None => command.registro.asInstanceOf[ObjetoExternalDto.ObjetosTri]
      }
      objetoFFF
    }

    val event = ObjetoEvents.ObjetoUpdatedFromTri(
      if (command.deliveryId.signum < 0) actor.state.lastDeliveryIdByEvents else command.deliveryId,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      getObjetoFFF(),
      command.isResponsable,
      command.sujetoResponsable,
      command.isAdheridoDebito,
      Some(dmn._1),
      Some(dmn._2)
    )

    if (isIdempotent(command, actor.state.lastDeliveryIdByEvents)) {
      log.error(
        s"[${actor.name} | ${actor.persistenceId}] -objeto- respond idempotent because of old delivery id | $command -> " + command.deliveryId + " <= " + actor.state.lastDeliveryIdByEvents
      )
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

  def persistSnapshotEvent(event: ObjetoUpdatedFromTri,
                           actor: ObjetoActor,
                           command: ObjetoUpdateFromTri,
                           requeriment: MonitoringAndMessageProducer): Success[Response.SuccessProcessing] = {
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

      if (actor.state.registro.get.SOJ_TIPO_OBJETO
            .equals("M")) { // todo tipo M , pero si para el calculo de deuda para un sujeto. Objeto juicio queda atado a cuit, pero no se va a teber en cuanta cuando se calcule el 30%, no se guarda el vinculo.
        if (actor.state.tiene30Objeto.equals(false)) {
          val res = actor.context.parent.ask[Response.SuccessProcessing](
            SujetoCommands.SujetoUpdateFromObjetoTreintaPorciento(
              command.deliveryId,
              command.sujetoId,
              command.objetoId,
              command.tipoObjeto,
              actor.state.saldo,
              actor.state.obligacionesSaldo.values.sum,
              actor.state.clasificacionObjeto
            )
          )
          res.onComplete {
            case Failure(exception) =>
              log.error(
                "Error to send event to sujeto tipo M false " + exception + " objID: " + command.objetoId + "sujID: " + command.sujetoId
              )
            case Success(value) =>
              log.debug(
                "Sent event to sujeto tipo M false " + " objID: " + command.objetoId + " sujID: " + command.sujetoId
              )
          }
        } else {
          {
            val res = actor.context.parent.ask[Response.SuccessProcessing](
              SujetoCommands.SujetoUpdateFromObjeto(
                command.deliveryId,
                command.sujetoId,
                command.objetoId,
                command.tipoObjeto,
                actor.state.saldo,
                actor.state.obligacionesSaldo.values.sum,
                actor.state.clasificacionObjeto
              )
            )
            res.onComplete {
              case Failure(exception) =>
                log.error(
                  "Error to send event to sujeto tipo M true " + exception + " objID: " + command.objetoId + "sujID: " + command.sujetoId
                )
              case Success(value) =>
                log.debug(
                  "Sent event to sujeto tipo M true " + " objID: " + command.objetoId + " sujID: " + command.sujetoId
                )
            }
          }
        }
        actor.persistSnapshot(event, actor.state) { () =>
          sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
        }
      } else {
        SendObjetoToObjetoVinculo(Obje,
                                  actor,
                                  command.sujetoId,
                                  command.objetoId,
                                  command.tipoObjeto,
                                  command.registro.SOJ_ESTADO,
                                  requeriment)
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
