package com.github.unchama.buildassist.domain.explevel

import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class BuildExpAmount(amount: BigDecimal) extends AnyVal {
  // TODO do not allow constructor invocation

  require {
    amount >= BigDecimal(0)
  }

  def mapAmount(f: BigDecimal => BigDecimal): BuildExpAmount = BuildExpAmount(f(amount))

  def add(a: BuildExpAmount): BuildExpAmount = mapAmount(_ + a.amount)

  def toPlainString: String = amount.setScale(1, BigDecimal.RoundingMode.HALF_UP).bigDecimal.toPlainString

}

private[explevel] abstract class BuildExpAmountInstances {
  implicit lazy val ordering: Ordering[BuildExpAmount] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[BuildExpAmount] = {
    HasMinimum.as(BuildExpAmount(0))
  }

  implicit lazy val orderedMonus: OrderedMonus[BuildExpAmount] = {
    new OrderedMonus[BuildExpAmount] {
      override def compare(x: BuildExpAmount, y: BuildExpAmount): Int =
        x.amount.compare(y.amount)

      override val empty: BuildExpAmount =
        BuildExpAmount(0)

      override def combine(x: BuildExpAmount, y: BuildExpAmount): BuildExpAmount =
        BuildExpAmount(x.amount + y.amount)

      override def |-|(x: BuildExpAmount, y: BuildExpAmount): BuildExpAmount = BuildExpAmount {
        (x.amount - y.amount) max BigDecimal(0)
      }
    }
  }
}

object BuildExpAmount extends BuildExpAmountInstances {
  def apply(amount: Int): BuildExpAmount = BuildExpAmount(BigDecimal(amount))
}
