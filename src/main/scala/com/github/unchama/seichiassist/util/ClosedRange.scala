package com.github.unchama.seichiassist.util

class ClosedRange[E](val start: E, val endInclusive: E)(implicit val ord: Ordering[E]) {

  import ord._

  def contains(another: E): Boolean = start <= another && another <= endInclusive

  def isEmpty: Boolean = start > endInclusive
}
