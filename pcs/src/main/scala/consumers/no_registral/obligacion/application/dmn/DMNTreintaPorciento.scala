package consumers.no_registral.obligacion.application.dmn

import consumers.no_registral.obligacion.application.entities.ObligacionesTri
import org.camunda.dmn.DmnEngine
import scalaz.concurrent.Task.Try

import java.io.FileInputStream

object DMNTreintaPorciento {


  def dmn(actor: ObligacionesTri) = {

    val dmnStream = Try(new FileInputStream("/opt/docker/bin/archivo.dmn"))
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

    chequeoDmn.fold(e => e, value => value)
  }
}

