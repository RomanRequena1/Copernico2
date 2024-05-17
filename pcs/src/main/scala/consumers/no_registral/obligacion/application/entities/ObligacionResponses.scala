package consumers.no_registral.obligacion.application.entities


import design_principles.actor_model.Response
import serialization.CbroSerialization

import java.time.LocalDateTime

sealed trait ObligacionResponses extends CbroSerialization
object ObligacionResponses {

  case class GetObligacionResponse(
      saldo: BigDecimal = 0,
      fechaUltMod: LocalDateTime = LocalDateTime.MIN,
      registro: Option[ObligacionExternalDto] = None,
      exenta: Boolean = false,
      porcentajeExencion: BigDecimal = 0,
      juicioId: Option[BigInt] = None
  ) extends Response with CbroSerialization
}
