package consumers.no_registral.obligacion.application.dmn

import consumers.no_registral.obligacion.application.entities.ObligacionExternalDto

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.{LocalDate, LocalDateTime}

object Utils {

  def diffDaysObligacion(ven: String): Long = {

    val formattedDate = LocalDateTime.parse(ven).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    val dateNow = java.time.LocalDate.now
    val dateBefore = LocalDate.parse(formattedDate)
    dateBefore.until(dateNow, ChronoUnit.DAYS) //todo hoy - pro o sino ven
  }


  def diffYearObligacion(state: String): Long = {
    val originalDateString = state.toInt
    val dateNow = LocalDate.now().getYear
    val diff = dateNow - originalDateString
    diff
  }

  def mapsToDMN(state: ObligacionExternalDto, is_vencida: Boolean, diffDaysObli: Long, diffYearObli: Long, diffDaysObliVen2: Long, dias_prescripcion: Long): Map[String, Any] = {
    val reg = state

    Map("bob_tipo"   -> reg.BOB_TIPO.getOrElse("None"),
      "bob_impuesto" -> reg.BOB_IMPUESTO.getOrElse("None"),
      "bob_concepto" -> reg.BOB_CONCEPTO.getOrElse("None"),
      "bob_estado"   -> reg.BOB_ESTADO.getOrElse("None"),
      "bob_capital"  -> reg.BOB_CAPITAL.getOrElse(0),
      "bob_saldo"    -> reg.BOB_SALDO.getOrElse(0),
      "is_vencida"   -> is_vencida,
      "dias_obligacion"     -> diffDaysObli,
      "years_obligacion"    -> diffYearObli,
      "bob_adherido_debito" -> reg.BOB_ADHERIDO_DEBITO.getOrElse("None"),
      "dias_vencimiento2"   -> diffDaysObliVen2,
      "dias_prescripcion"   -> dias_prescripcion)
  }
}


