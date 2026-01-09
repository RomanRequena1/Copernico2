package monitoring

import kamon.Kamon

class KamonMonitoring extends Monitoring {

  // init Kamon
  Kamon.init()

  override def counter(name: String, context: Map[String, String] = Map.empty): Counter =
    new KamonCounter(name, context)

  override def histogram(name: String): Histogram = new KamonHistogram(name)

  override def gauge(name: String, context: Map[String, String] = Map.empty): Gauge = new KamonGauge(name, context)

  val bsobjt: String = Option(System.getenv("BETTER_SORTER_OBJETO_TRI")).getOrElse("OFF")
  val bsobja: String = Option(System.getenv("BETTER_SORTER_OBJETO_ANT")).getOrElse("OFF")
  val bsobna: String = Option(System.getenv("BETTER_SORTER_OBLIGACION_ANT")).getOrElse("OFF")
  val bsobnt: String = Option(System.getenv("BETTER_SORTER_OBLIGACION_TRI")).getOrElse("OFF")
  val spobnt: String = Option(System.getenv("STATE_PARCIAL_OBLIGACION_TRI")).getOrElse("OFF")
  val spobna: String = Option(System.getenv("STATE_PARCIAL_OBLIGACION_ANT")).getOrElse("OFF")
  val spobjt: String = Option(System.getenv("STATE_PARCIAL_OBJETO_TRI")).getOrElse("OFF")
  val spobja: String = Option(System.getenv("STATE_PARCIAL_OBJETO_ANT")).getOrElse("OFF")

  val cbsobjt = gauge("better_sorter_objeto_tri")
  val cbsobja = gauge("better_sorter_objeto_ant")
  val cbsobna = gauge("better_sorter_obligacion_ant")
  val cbsobnt = gauge("better_sorter_obligacion_tri")
  val cspobnt = gauge("state_parcial_obligacion_tri")
  val cspobna = gauge("state_parcial_obligacion_ant")
  val cspobjt = gauge("state_parcial_objeto_tri")
  val cspobja = gauge("state_parcial_objeto_ant")

  bsobjt match {
    case "ON" => cbsobjt.set(1)
    case _ => cbsobjt.set(0)
  }
  bsobja match {
    case "ON" => cbsobja.set(1)
    case _ => cbsobja.set(0)
  }

  bsobna match {
    case "ON" => cbsobna.set(1)
    case _ => cbsobna.set(0)
  }
  bsobnt match {
    case "ON" => cbsobnt.set(1)
    case _ => cbsobnt.set(0)
  }
  spobnt match {
    case "ON" => cspobnt.set(1)
    case _ => cspobnt.set(0)
  }
  spobna match {
    case "ON" => cspobna.set(1)
    case _ => cspobna.set(0)
  }
  spobjt match {
    case "ON" => cspobjt.set(1)
    case _ => cspobjt.set(0)
  }
  spobja match {
    case "ON" => cspobja.set(1)
    case _ => cspobja.set(0)
  }

//  private val mappings: Map[String, Gauge] = Map(
//    bsobjt -> cbsobjt,
//    bsobja -> cbsobja,
//    bsobna -> cbsobna,
//    bsobnt -> cbsobnt,
//    spobnt -> cspobnt,
//    spobna -> cspobna,
//    spobjt -> cspobjt,
//    spobja -> cspobja
//  )
//
//  mappings.foreach { case (state, gauge) =>
//    gauge.set(if (state == "ON") 1 else 0)
//  }

}
