package consumers.no_registral.obligacion.application.dmn

import consumers.no_registral.obligacion.application.entities.{ObligacionExternalDto, ObligacionesTri}
import org.camunda.dmn.DmnEngine
import org.slf4j.LoggerFactory
import scalaz.concurrent.Task.Try

import java.io.FileInputStream

object DMNTreintaPorciento {

  private val log = LoggerFactory.getLogger(this.getClass)

  def dmn(actor: ObligacionExternalDto): Any = {


    val dmnStream = Try(new FileInputStream("/opt/docker/bin/decision_30_descuento.dmn"))
    val engine = new DmnEngine()

    val ven = actor.BOB_VENCIMIENTO.get.toString.replace(" ", "T")
    val ven2 = actor.BOB_VENCIMIENTO_2.get.toString.replace(" ", "T")

    val diffDaysOblligaciones = Utils.diffDaysObligacion(ven)

    val diffDaysOblligacionesVen2 = Utils.diffDaysObligacion(ven2)

    val diffYearsOblligaciones = Utils.diffYearObligacion(actor.BOB_PERIODO.get)

    val isVencida = if (diffDaysOblligaciones > 10) true else false
    println("isvalid - " + isVencida + " - diff days - " + diffDaysOblligaciones + " - year - " + diffYearsOblligaciones + " - " + Utils.mapsToDMN(actor, isVencida, diffDaysOblligaciones, diffYearsOblligaciones, diffDaysOblligacionesVen2))

    val chequeoDmn: Either[Product, DmnEngine.EvalResult] = engine.parse(dmnStream.getOrElse(null))
      .flatMap(dmn => engine.eval(dmn, "Decision_descuento", Utils.mapsToDMN(actor, isVencida, diffDaysOblligaciones, diffYearsOblligaciones, diffDaysOblligacionesVen2)))
    chequeoDmn.fold(e => log.error("ERROR DMN OBLIGACION::" + e), value => value)
  }
}

