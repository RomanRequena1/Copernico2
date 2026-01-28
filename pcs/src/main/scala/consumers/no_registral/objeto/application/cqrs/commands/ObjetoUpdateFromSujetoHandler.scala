package consumers.no_registral.objeto.application.cqrs.commands

import akka.actor.ActorRef
import akka.entity.ShardedEntity.MonitoringAndMessageProducer
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal
import consumers.no_registral.objeto.application.dmn.DMNTreintaPorcientoFinal.DmnFinal
import consumers.no_registral.objeto.application.entities.ObjetoCommands
import consumers.no_registral.objeto.application.entities.ObjetoExternalDto.ObjetosTri
import consumers.no_registral.objeto.domain.ObjetoEvents.{AplicarDescuentoUpdated, DmnResumen, ObjetoUpdatedFromSujeto}
import consumers.no_registral.objeto.infrastructure.dependency_injection.ObjetoActor
import consumers.no_registral.tranferencia.application.entity.ObjetoVinculoCommands
import consumers.no_registral.tranferencia.infrastructure.dependency_injection.ObjetoVinculoActor
import cqrs.untyped.command.CommandHandler.SyncCommandHandler
import design_principles.actor_model.Response

import scala.util.{Success, Try}

class ObjetoUpdateFromSujetoHandler(actor: ObjetoActor, requeriment: MonitoringAndMessageProducer)
  extends SyncCommandHandler[ObjetoCommands.ObjetoUpdateFromSujeto] {

  private val resumenEnabled: Option[String] = Option(System.getenv("KAFKA_BROKERS_LIST_PSRM"))

  override def handle(command: ObjetoCommands.ObjetoUpdateFromSujeto): Try[Response.SuccessProcessing] = {
    val sender = actor.context.sender()

    val obj_default: ObjetosTri = ObjetosTri(
      Some("None"), 0, "None", "None", "None", Some("None"), Some("None"), Some("None"),
      None, None, Some("None"), None, Some(0), Some("None"), Some(0), Some("None"),
      Some("None"), Some("None"), Some("None"), Some("None"), Some("None"),
      None, None
    )

    val estado = actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("")
    if (estado == "TRANSF" || estado == "ESTADO2") {
      sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      return Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
    }

    val event = ObjetoUpdatedFromSujeto(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      command.tiene30Sujeto,
      command.exclusionSUjeto,
      command.dmnDescripcionSujeto,
      command.idExterno,
      command.deliveryIdObligacion
    )

    actor.state += event

    def esTipoObjetoPermitido(tipo: String): Boolean =
      Set("A", "I", "N").contains(tipo)

    // Justo antes de calcular el DMN, agregá:
    println(s"[DEBUG-DMN-INPUT] objetoId=${command.objetoId}, sujetoId=${command.sujetoId}")
    println(s"  exclusionSujeto=${command.exclusionSUjeto}")
    println(s"  exclusionObjeto=${actor.state.exclusionObjeto}")
    println(s"  clasificacionObjeto=${actor.state.clasificacionObjeto}")
    println(s"  tiene30Objeto=${actor.state.tiene30Objeto}")
    println(s"  tiene30Sujeto=${actor.state.tiene30Sujeto}")
    println(s"  tiene30ObjetoVinculo=${actor.state.tiene30ObjetoVinculo}")

    val result: Boolean = DMNTreintaPorcientoFinal.calcularDmnFinal(
      DmnFinal(
        command.exclusionSUjeto,
        actor.state.exclusionObjeto,
        actor.state.clasificacionObjeto,
        actor.state.tiene30Objeto,
        actor.state.tiene30Sujeto.get,
        actor.state.tiene30ObjetoVinculo
      )
    )

    val aplicarDescuentoNuevo = Some(result)

    // CLAVE: Capturar el valor ANTERIOR antes de actualizar
    val aplicarDescuentoAnterior = actor.state.aplicarDescuento

    val event1 = AplicarDescuentoUpdated(
      if (actor.state.lastDeliveryIdByEvents.equals(0)) 0 else actor.state.lastDeliveryIdByEvents,
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      aplicarDescuentoNuevo
    )

    actor.state += event1

    // Validar si cambió la marca A NIVEL DE OBJETO
    val cambioDeMarcaEnObjeto = aplicarDescuentoAnterior != aplicarDescuentoNuevo

    // --- Detectar cuando el objeto pierde el descuento POR deuda en OTRO objeto
    // isLossByOther:
    //  - la marca cambió de true -> false (aplicarDescuentoAnterior true, aplicarDescuentoNuevo false)
    //  - y el objeto NO tiene deuda propia (tiene30Objeto == true)
    val isLossByOther = cambioDeMarcaEnObjeto &&
      aplicarDescuentoAnterior.contains(true) &&
      aplicarDescuentoNuevo.contains(false) &&
      actor.state.tiene30Objeto

    // CORRECCION: Lógica de DMN para auditoría
    // La descripción debe ser CONSISTENTE con aplicarDescuentoNuevo (el resultado final)
    //
    // Casos:
    // 1) isLossByOther => (99, "No cumple por deuda en otro objeto del sujeto")
    // 2) Si hay DMN en estado => usar ese (ya viene del cálculo de obligación)
    // 3) Si no hay DMN en estado:
    //    - Si aplicarDescuentoNuevo=true => "No Deuda" (cumple el 30%)
    //    - Si aplicarDescuentoNuevo=false => usar descripción del sujeto o "Deuda"
    val (dmnNumeroParaPSRM, dmnDescripcionParaPSRM) = {
      if (isLossByOther) {
        // Caso especial: pierde por deuda en otro objeto
        (Some(99), Some("No cumple por deuda en otro objeto del sujeto"))
      } else if (actor.state.dmnNumero.isDefined) {
        // Si ya tiene DMN calculado, usarlo
        // PERO validar que la descripción sea consistente con aplicarDescuentoNuevo
        val descExistente = actor.state.dmnDescripcion
        val descConsistente = validarDescripcionConsistente(
          aplicarDescuentoNuevo,
          descExistente,
          command.dmnDescripcionSujeto
        )
        (actor.state.dmnNumero, descConsistente)
      } else {
        // No hay DMN en estado - determinar basándose en aplicarDescuentoNuevo
        // CORRECCION: Antes usaba tiene30Objeto, ahora usa aplicarDescuentoNuevo
        if (result) {
          // Cumple el 30% -> "No Deuda"
          (Some(1), Some("No Deuda"))
        } else {
          // NO cumple el 30% -> usar descripción del sujeto si existe, sino "Deuda"
          val desc = command.dmnDescripcionSujeto
            .filter(_.nonEmpty)
            .getOrElse("Deuda")
          (Some(0), Some(desc))
        }
      }
    }

    // Log para debugging de inconsistencias
    if (cambioDeMarcaEnObjeto) {
      log.info(
        s"[DMN-DEBUG] objetoId=${command.objetoId}, sujetoId=${command.sujetoId} - " +
          s"aplicarDescuento: $aplicarDescuentoAnterior -> $aplicarDescuentoNuevo, " +
          s"tiene30Objeto=${actor.state.tiene30Objeto}, " +
          s"tiene30Sujeto=${actor.state.tiene30Sujeto}, " +
          s"tiene30ObjetoVinculo=${actor.state.tiene30ObjetoVinculo}, " +
          s"dmnNumero=$dmnNumeroParaPSRM, dmnDescripcion=$dmnDescripcionParaPSRM"
      )
    }

    val eventDmn = DmnResumen(
      command.deliveryIdObligacion.getOrElse(0),
      command.sujetoId,
      command.objetoId,
      command.tipoObjeto,
      actor.state.idExterno,
      Some(actor.state.fechaUltMod),
      actor.state.aplicarDescuento,
      dmnNumeroParaPSRM,
      dmnDescripcionParaPSRM
    )

    // Solo enviar a auditoría SI cambió la marca EN ESTE ObjetoActor
    if (!actor.state.registro.getOrElse(obj_default).SOJ_ESTADO.getOrElse("").equals("BAJA") &&
      actor.state.aplicarDescuento.isDefined &&
      esTipoObjetoPermitido(command.tipoObjeto) &&
      resumenEnabled.isDefined &&
      cambioDeMarcaEnObjeto &&
      command.objetoId.nonEmpty)
    {
      actor.persistSnapshot(event, actor.state) { () =>
        implicit val system = actor.context.system
        val vinculoActor: ActorRef = ObjetoVinculoActor.startWithRequirements(requeriment)

        log.info(s"[AUDITORIA-FROM-SUJETO] Enviando - objetoId=${command.objetoId}, sujetoId=${command.sujetoId}, " +
          s"anterior=$aplicarDescuentoAnterior, nuevo=$aplicarDescuentoNuevo, " +
          s"dmnDesc=$dmnDescripcionParaPSRM")

        vinculoActor ! ObjetoVinculoCommands.AuditarYEnviarResumen(
          deliveryId = command.deliveryId,
          objetoId = command.objetoId,
          tipoObj = command.tipoObjeto,
          sujetoId = command.sujetoId,
          aplicarDescuento = actor.state.aplicarDescuento,
          idExterno = command.idExterno,
          eventDmn = eventDmn
        )

        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    } else {
      log.info(s"[AUDITORIA-SKIP] objetoId=${command.objetoId}, sujetoId=${command.sujetoId}, " +
        s"cambioDeMarca=$cambioDeMarcaEnObjeto, anterior=$aplicarDescuentoAnterior, nuevo=$aplicarDescuentoNuevo")

      actor.persistSnapshot(event, actor.state) { () =>
        sender ! Response.SuccessProcessing(command.aggregateRoot, command.deliveryId)
      }
    }

    Success(Response.SuccessProcessing(command.aggregateRoot, command.deliveryId))
  }

  /**
   * CORRECCION: Valida que la descripción sea consistente con aplicarDescuento.
   * Si hay inconsistencia, retorna una descripción corregida.
   *
   * @param aplicarDescuento El resultado final del cálculo (true = cumple 30%, false = no cumple)
   * @param descExistente La descripción que viene del estado
   * @param descSujeto La descripción que viene del sujeto (fallback)
   * @return Descripción consistente con aplicarDescuento
   */
  private def validarDescripcionConsistente(
                                             aplicarDescuento: Option[Boolean],
                                             descExistente: Option[String],
                                             descSujeto: Option[String]
                                           ): Option[String] = {
    val desc = descExistente.getOrElse("").toLowerCase

    (aplicarDescuento, desc) match {
      // Caso inconsistente: aplicarDescuento=false pero descripción dice "No Deuda"
      case (Some(false), d) if d.contains("no deuda") =>
        log.warn(
          s"[DMN-INCONSISTENCIA-CORREGIDA] aplicarDescuento=false pero descripcion='$descExistente' - " +
            s"usando descripcion del sujeto o 'Deuda'"
        )
        // Usar descripción del sujeto si existe, sino "Deuda"
        descSujeto.filter(_.nonEmpty).orElse(Some("Deuda"))

      // Caso inconsistente: aplicarDescuento=true pero descripción dice "Deuda" (sin "No")
      case (Some(true), d) if d.contains("deuda") && !d.contains("no deuda") =>
        log.warn(
          s"[DMN-INCONSISTENCIA-CORREGIDA] aplicarDescuento=true pero descripcion='$descExistente' - " +
            s"usando 'No Deuda'"
        )
        Some("No Deuda")

      // Caso consistente: mantener descripción existente
      case _ =>
        descExistente
    }
  }
}