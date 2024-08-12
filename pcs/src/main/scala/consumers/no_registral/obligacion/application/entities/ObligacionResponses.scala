package consumers.no_registral.obligacion.application.entities

import design_principles.actor_model.Response
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ObligacionResponses extends CbroSerialization
object ObligacionResponses {

  case class GetObligacionResponse(
      saldo: BigDecimal = 0,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      exenta: Boolean = false,
      porcentajeExencion: Option[BigDecimal] = None,
      registro: Option[ObligacionExternalDto] = None,
      lastDeliveryIdByEvents: BigInt = 0,
      detallesObligacion: Seq[DetallesObligacion] = Seq.empty,
      detallesSupresiones: Option[Seq[DetallesSupresiones]] = None,
      juicioId: Option[BigInt] = None,
      isAdheridoDebito: Boolean = false,
      eventCounter: Int = 0,
      idExterno: Option[String] = None,
      resultDmn: Option[String] = None
  ) extends Response
      with CbroSerialization
}
