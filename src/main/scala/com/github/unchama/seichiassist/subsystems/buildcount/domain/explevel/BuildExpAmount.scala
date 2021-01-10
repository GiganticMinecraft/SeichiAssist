package com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel

import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class BuildExpAmount private(amount: BigDecimal) extends AnyVal {

  def mapAmount(f: BigDecimal => BigDecimal): BuildExpAmount = BuildExpAmount.ofNonNegative(f(amount))

  def add(a: BuildExpAmount): BuildExpAmount = mapAmount(_ + a.amount)

  def toPlainString: String = amount.setScale(1, BigDecimal.RoundingMode.HALF_UP).bigDecimal.toPlainString

}

private[explevel] abstract class BuildExpAmountInstances {
  implicit lazy val ordering: Ordering[BuildExpAmount] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[BuildExpAmount] = {
    HasMinimum.as(BuildExpAmount.ofNonNegative(0))
  }

  implicit lazy val orderedMonus: OrderedMonus[BuildExpAmount] = {
    new OrderedMonus[BuildExpAmount] {
      override def compare(x: BuildExpAmount, y: BuildExpAmount): Int =
        x.amount.compare(y.amount)

      override val empty: BuildExpAmount =
        BuildExpAmount.ofNonNegative(0)

      override def combine(x: BuildExpAmount, y: BuildExpAmount): BuildExpAmount =
        BuildExpAmount.ofNonNegative(x.amount + y.amount)

      override def |-|(x: BuildExpAmount, y: BuildExpAmount): BuildExpAmount = BuildExpAmount.ofNonNegative {
        (x.amount - y.amount) max BigDecimal(0)
      }
    }
  }
}

object BuildExpAmount extends BuildExpAmountInstances {
  def ofNonNegative(amount: BigDecimal): BuildExpAmount = {
    require(
      amount >= BigDecimal(0),
      "建築経験値量は非負である必要があります。"
    )

    BuildExpAmount(amount)
  }

  def ofNonNegative(amount: Int): BuildExpAmount = ofNonNegative(BigDecimal(amount))

  private def apply(amount: Int): BuildExpAmount = BuildExpAmount(BigDecimal(amount))
}
