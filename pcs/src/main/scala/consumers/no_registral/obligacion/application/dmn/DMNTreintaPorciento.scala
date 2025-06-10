package consumers.no_registral.obligacion.application.dmn

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.helper.FileStreamDmn.dmnStream
import org.camunda.dmn.DmnEngine
import org.slf4j.LoggerFactory
import scalaz.Scalaz.ToValidationOps
import scalaz.concurrent.Task.Try

object DMNTreintaPorciento {

  private val log = LoggerFactory.getLogger(this.getClass)

  def dmn(actor: ObligacionExternalDto): Option[(Int, String)] = {

    val dmnId: String = Try(System.getenv("DMN_ID_DECISION_30")).getOrElse("id")

    val ven = actor.BOB_VENCIMIENTO.get.toString.replace(" ", "T")

    val venPro = actor.BOB_PRORROGA.getOrElse(ven).toString.replace(" ", "T")

    val ven2 = actor.BOB_VENCIMIENTO_2.getOrElse(ven).toString.replace(" ", "T") // todo revisar si esta bien cargado (MUC)

    val vencimientoMayor: String = if (Utils.diffDaysObligacion(ven) < Utils.diffDaysObligacion(venPro)) ven else venPro

    val diffDaysOblligaciones: Long = Utils.diffDaysObligacion(vencimientoMayor)

    val diffDaysOblligacionesVen2 = Utils.diffDaysObligacion(ven2)

    val diffYearsOblligaciones = Utils.diffYearObligacion(actor.BOB_PERIODO.get)

    val dias_prescripcion = diffDaysOblligaciones

    val isVencida = if (diffDaysOblligaciones > 10) true else false

    val inputMap = Utils.mapsToDMN(actor, isVencida, diffDaysOblligaciones, diffYearsOblligaciones, diffDaysOblligacionesVen2, dias_prescripcion)

    dmnStream
      .flatMap(dmn =>
        new DmnEngine().eval(dmn, dmnId, inputMap)
      ).fold(
        e => {
          log.error("ERROR DMN OBLIGACION::" + e); None
        },
        value => value.value match {
          case map: Map[String, Any] =>
            val numero = map.get("decision_30_descuento")
              .orElse(map.values.collectFirst { case i: Int => i })
              .collect { case i: java.lang.Number => i.intValue }
            val desc = map.get("descripcion")
              .orElse(map.values.collectFirst { case s: String if s.nonEmpty => s })
              .map(_.toString)
              .getOrElse("")
            numero.map(n => (n, desc))
          case l: List[Map[String, Any]] if l.nonEmpty =>
            val map = l.head
            val numero = map.get("decision_30_descuento")
              .orElse(map.values.collectFirst { case i: Int => i })
              .collect { case i: java.lang.Number => i.intValue }
            val desc = map.get("descripcion")
              .orElse(map.values.collectFirst { case s: String if s.nonEmpty => s })
              .map(_.toString)
              .getOrElse("")
            numero.map(n => (n, desc))
          case otro =>
            log.error(s"Formato inesperado: $otro")
            None
        }
      )
  }
}
