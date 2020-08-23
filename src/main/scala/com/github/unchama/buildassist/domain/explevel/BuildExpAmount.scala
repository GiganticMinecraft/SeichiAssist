package com.github.unchama.buildassist.domain.explevel

import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class BuildExpAmount private(amount: BigDecimal) extends AnyVal

private[explevel] abstract class BuildExpAmountInstances {
  implicit lazy val ordering: Ordering[BuildExpAmount] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[BuildExpAmount] = {
    HasMinimum.as(BuildExpAmount.ofNonNegative(0))
  }
}

object BuildExpAmount extends BuildExpAmountInstances {
  def ofNonNegative(amount: BigDecimal): BuildExpAmount = {
    require(amount >= 0)
    BuildExpAmount(amount)
  }
}
