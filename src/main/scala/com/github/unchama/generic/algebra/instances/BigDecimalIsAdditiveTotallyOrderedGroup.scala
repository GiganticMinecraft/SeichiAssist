package com.github.unchama.generic.algebra.instances

import com.github.unchama.generic.algebra.typeclasses.TotallyOrderedGroup

object BigDecimalIsAdditiveTotallyOrderedGroup extends TotallyOrderedGroup[BigDecimal] {
  override def compare(x: BigDecimal, y: BigDecimal): Int = x.compare(y)

  override def inverse(a: BigDecimal): BigDecimal = -a

  override def empty: BigDecimal = BigDecimal.apply(0)

  override def combine(x: BigDecimal, y: BigDecimal): BigDecimal = x + y
}
