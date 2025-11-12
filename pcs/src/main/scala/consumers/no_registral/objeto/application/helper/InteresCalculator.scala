package consumers.no_registral.objeto.application.helper

import java.time.{LocalDate, LocalDateTime}
import java.time.temporal.ChronoUnit

case class IndiceRecargo(id: Int, fechaDesde: LocalDate, fechaHasta: LocalDate, valor: BigDecimal)

object InteresCalculator {



  /*
    [{'fechaDesde': '2002-09-01','fechaHasta': '2003-10-31','valor': 0.1300},
    {'fechaDesde': '2003-11-01','fechaHasta': '2014-03-31','valor': 0.0830},
    {'fechaDesde': '2014-04-01','fechaHasta': '2022-06-30','valor': 0.1},
    {'fechaDesde': '2022-07-01','fechaHasta': '2022-08-31','valor': 0.141667},
    {'fechaDesde': '2022-09-01','fechaHasta': '2024-01-31','valor': 0.197000},
    {'fechaDesde': '2024-02-01','fechaHasta': '2024-03-31','valor': 0.509000},
    {'fechaDesde': '2024-04-01','fechaHasta': '2024-05-31','valor': 0.402333},
    {'fechaDesde': '2024-06-01','fechaHasta': '2024-11-30','valor': 0.213667},
    {'fechaDesde': '2024-12-01','fechaHasta': '2025-01-31','valor': 0.249000},
    {'fechaDesde': '2025-02-01','fechaHasta': '2025-02-28','valor': 0.242000},
    {'fechaDesde': '2025-03-01','fechaHasta': '2025-06-30','valor': 0.133333},
    {'fechaDesde': '2025-07-01','fechaHasta': '2099-12-31','valor': 0.091666}]
  */
//  private val parametrosDefault2 = List(
//    IndiceRecargo(LocalDate.of(2002, 9, 1), LocalDate.of(2003, 10, 31), BigDecimal("0.13")),
//    IndiceRecargo(LocalDate.of(2003, 11, 1), LocalDate.of(2014, 3, 31), BigDecimal("0.083")),
//    IndiceRecargo(LocalDate.of(2014, 4, 1), LocalDate.now(), BigDecimal("0.1"))
//  )
  private val parametrosDefault = List(
    IndiceRecargo(1, LocalDate.of(2002, 9, 1), LocalDate.of(2003, 10, 31), BigDecimal("0.1300")),
    IndiceRecargo(2, LocalDate.of(2003, 11, 1), LocalDate.of(2014, 3, 31), BigDecimal("0.0830")),
    IndiceRecargo(3, LocalDate.of(2014, 4, 1), LocalDate.of(2022, 6, 30), BigDecimal("0.1")),
    IndiceRecargo(4, LocalDate.of(2022, 7, 1), LocalDate.of(2022, 8, 31), BigDecimal("0.141667")),
    IndiceRecargo(5, LocalDate.of(2022, 9, 1), LocalDate.of(2024, 1, 31), BigDecimal("0.197000")),
    IndiceRecargo(6, LocalDate.of(2024, 2, 1), LocalDate.of(2024, 3, 31), BigDecimal("0.509000")),
    IndiceRecargo(7, LocalDate.of(2024, 4, 1), LocalDate.of(2024, 5, 31), BigDecimal("0.402333")),
    IndiceRecargo(8, LocalDate.of(2024, 6, 1), LocalDate.of(2024, 11, 30), BigDecimal("0.213667")),
    IndiceRecargo(9, LocalDate.of(2024, 12, 1), LocalDate.of(2025, 1, 31), BigDecimal("0.249000")),
    IndiceRecargo(10, LocalDate.of(2025, 2, 1), LocalDate.of(2025, 2, 28), BigDecimal("0.242000")),
    IndiceRecargo(11, LocalDate.of(2025, 3, 1), LocalDate.of(2025, 6, 30), BigDecimal("0.133333")),
    IndiceRecargo(12, LocalDate.of(2025, 7, 1), LocalDate.now(), BigDecimal("0.091666"))
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
        println(s"Fecha: $fechaVencPro")
        val daysDiff = ChronoUnit.DAYS.between(fechaVencPro, param.fechaHasta)
        interesRecargo += ((param.valor / 100) * capital * daysDiff).setScale(2, BigDecimal.RoundingMode.HALF_UP)
        println(s"Debug - Param.valor: ${param.valor}, Capital: $capital, DaysDiff: $daysDiff")
        println(s"Debug - ${param.valor / 100}")
        println(s"Debug - ${(param.valor / 100) * capital}")
        println(s"Debug - ${(param.valor / 100) * capital * daysDiff}")
        println(s"Saldo: $capital | Interes recargo: $interesRecargo | ID: ${param.id} - ${param.valor}")
        flag = false
      } else if (!flag) {
        val daysDiff = ChronoUnit.DAYS.between(param.fechaDesde, param.fechaHasta) + 1
        interesRecargo += ((param.valor / 100) * capital * daysDiff).setScale(2, BigDecimal.RoundingMode.HALF_UP)
        println(s"Saldo: $capital | Interes recargo: $interesRecargo | ID: ${param.id} - ${param.valor}")
      }
    }

    if (saldo.isDefined && interesRecargo > 0 && estado.contains("PREJUDICIAL")) {
      interesRecargo
    } else {
      interesRecargo
    }
  }
}