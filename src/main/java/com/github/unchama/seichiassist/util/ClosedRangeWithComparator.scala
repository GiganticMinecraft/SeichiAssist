package com.github.unchama.seichiassist.util

import java.util.Comparator

class ClosedRangeWithComparator[E](val start: E, val endInclusive: E, val comparator: Comparator[E]) {

  def contains(another: E): Boolean = {
    // start <= another <= endInclusive
    comparator.compare(start, another) <= 0 && comparator.compare(another, endInclusive) <= 0
  }

  def isEmpty(): Boolean = comparator.compare(start, endInclusive) > 0

}