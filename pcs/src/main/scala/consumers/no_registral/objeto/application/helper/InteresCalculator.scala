package consumers.no_registral.objeto.application.helper

import java.time.{LocalDate, LocalDateTime}
import java.time.temporal.ChronoUnit

case class IndiceRecargo(fechaDesde: LocalDate, fechaHasta: LocalDate, valor: BigDecimal)

object InteresCalculator {

  private val parametrosDefault = List(
    IndiceRecargo(LocalDate.of(2002, 9, 1), LocalDate.of(2003, 10, 31), BigDecimal("0.13")),
    IndiceRecargo(LocalDate.of(2003, 11, 1), LocalDate.of(2014, 3, 31), BigDecimal("0.083")),
    IndiceRecargo(LocalDate.of(2014, 4, 1), LocalDate.now(), BigDecimal("0.1"))
  )

  def aplicarInteres(
                      capital: BigDecimal,
                      vencimiento: Option[LocalDateTime],
                      prorroga: Option[LocalDateTime],
                      estado: Option[String],
                      saldo: Option[BigDecimal],
                      parametros: List[IndiceRecargo] = parametrosDefault
                    ): BigDecimal = {

    val fechaVencPro = (prorroga, vencimiento) match {
      case (Some(p), _) if LocalDate.now().isAfter(p.toLocalDate) =>
        vencimiento.map(_.toLocalDate).getOrElse(LocalDate.of(1900, 1, 1))
      case (Some(p), _) => p.toLocalDate
      case (None, Some(v)) => v.toLocalDate
      case _ => LocalDate.of(1900, 1, 1)
    }

    var flag = true
    var interesRecargo = BigDecimal(0)

    parametros.foreach { param =>
      if (!fechaVencPro.isBefore(param.fechaDesde) && !fechaVencPro.isAfter(param.fechaHasta)) {
        val daysDiff = ChronoUnit.DAYS.between(fechaVencPro, param.fechaHasta)
        interesRecargo += ((param.valor / 100) * capital * daysDiff).setScale(2, BigDecimal.RoundingMode.HALF_UP)
        flag = false
      } else if (!flag) {
        val daysDiff = ChronoUnit.DAYS.between(param.fechaDesde, param.fechaHasta) + 1
        interesRecargo += ((param.valor / 100) * capital * daysDiff).setScale(2, BigDecimal.RoundingMode.HALF_UP)
      }
    }

    if (saldo.isDefined && interesRecargo > 0 && estado.contains("PREJUDICIAL")) {
      interesRecargo
    } else {
      interesRecargo
    }
  }
}