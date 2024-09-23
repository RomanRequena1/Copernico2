package consumers.no_registral.obligacion.application.dmn

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto
import consumers.no_registral.obligacion.application.helper.FileStreamDmn.dmnStream
import org.camunda.dmn.DmnEngine
import org.slf4j.LoggerFactory
import scalaz.Scalaz.ToValidationOps
import scalaz.concurrent.Task.Try

object DMNTreintaPorciento {

  private val log = LoggerFactory.getLogger(this.getClass)
  def dmn(actor: ObligacionExternalDto): Any = {

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

    val engine = new DmnEngine()

    dmnStream
      .flatMap(dmn =>
        engine.eval(dmn,
                    dmnId,
                    Utils.mapsToDMN(actor,
                                    isVencida,
                                    diffDaysOblligaciones,
                                    diffYearsOblligaciones,
                                    diffDaysOblligacionesVen2,
                                    dias_prescripcion))
      )
      .fold(e => log.error("ERROR DMN OBLIGACION::" + e), value => value.value)
  }

}
