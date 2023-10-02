package readside.proyectionists.registrales.parametrica_plan.projections

import consumers.registral.parametrica_plan.application.entities.{ ParametricaPlanTri}
import consumers.registral.parametrica_plan.domain.ParametricaPlanEvents

case class ParametricaPlanUpdatedFromDtoProjection(
    event: ParametricaPlanEvents.ParametricaPlanUpdatedFromDto
) extends ParametricaPlanProjection {
  val registro: ParametricaPlanTri = event.registro

  def bindings: List[(String, Option[String])] = List(
    "bpp_decreto" -> registro.BPP_DECRETO
  )
}
