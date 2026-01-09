package monitoring

import kamon.Kamon
import kamon.tag.TagSet

class KamonGauge(name: String, context: Map[String, String]) extends Gauge {
  private val tags = TagSet.from(context + ("entity" -> name))

  private val gauge = Kamon
    .gauge("copernico-gauges")
    .withTags(tags)


  override def increment(): Unit = gauge.increment()
  override def decrement(): Unit = gauge.decrement()

  override def add(num: Int): Unit = gauge.increment(num)
  override def subtract(num: Int): Unit = gauge.decrement(num)

  override def set(num: Int): Unit = gauge.update(num)
  override def set(num: Long): Unit = gauge.update(num)

}
