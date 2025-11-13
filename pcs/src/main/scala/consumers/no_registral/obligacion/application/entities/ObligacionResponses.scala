package consumers.no_registral.obligacion.application.entities

import design_principles.actor_model.Response
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ObligacionResponses extends CbroSerialization
object ObligacionResponses {

  case class GetObligacionResponse(
      saldo: BigDecimal = 0,
      interes: BigDecimal = 0,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      exenta: Boolean = false,
      porcentajeExencion: Option[BigDecimal] = None,
      registro: Option[ObligacionExternalDto] = None,
      lastDeliveryIdByEvents: BigInt = 0,
      detallesObligacion: Seq[DetallesObligacion] = Seq.empty,
      detallesCaracteristicas: Seq[DetallesObligacionCaracteristicas] = Seq.empty,
      detallesSupresiones: Option[Seq[DetallesSupresiones]] = None,
      juicioId: Option[BigInt] = None,
      isAdheridoDebito: Boolean = false,
      eventCounter: Int = 0,
      idExterno: Option[String] = None,
      resultDmn: Option[String] = None
  ) extends Response
      with CbroSerialization

  case class GetMiniObligacionResponse(id: String,
                                       saldo:Option[BigDecimal],
                                       vencimiento: Option[LocalDateTime],
                                       estado: Option[String],
                                       tiene30: Option[Boolean]
                                      ) extends Response with CbroSerialization
}
