package com.github.unchama.util.instances

import java.util.GregorianCalendar

trait GregorianCalendarInstances {
  implicit val instanceForGregorianCalendarOrdering: Ordering[GregorianCalendar] = new GregorianCalendarOrdering
}

class GregorianCalendarOrdering extends Ordering[GregorianCalendar] {
  override def compare(x: GregorianCalendar, y: GregorianCalendar): Int =
    x.getTimeInMillis.compare(y.getTimeInMillis)
}