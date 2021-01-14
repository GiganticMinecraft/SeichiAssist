package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import cats.kernel.{LowerBounded, PartialOrder}
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus

/**
 * 整地量を表す値のクラス。非負の値に対応する。
 */
case class SeichiExpAmount private(amount: Long) extends AnyVal {

  def mapAmount(f: Long => Long): SeichiExpAmount =
    SeichiExpAmount.ofNonNegative(f(amount))

  def add(a: SeichiExpAmount): SeichiExpAmount = mapAmount(_ + a.amount)

}

object SeichiExpAmount {
  def ofNonNegative(amount: Long): SeichiExpAmount = {
    require(
      amount >= 0L,
      "整地経験値量は非負である必要があります。"
    )

    SeichiExpAmount(amount)
  }

  lazy val zero: SeichiExpAmount = ofNonNegative(0)

  // region instances
  implicit lazy val orderedMonus: OrderedMonus[SeichiExpAmount] = new OrderedMonus[SeichiExpAmount] {
    override def |-|(x: SeichiExpAmount, y: SeichiExpAmount): SeichiExpAmount = ofNonNegative {
      if (x.amount > y.amount) x.amount - y.amount else 0
    }

    override def empty: SeichiExpAmount = zero

    override def combine(x: SeichiExpAmount, y: SeichiExpAmount): SeichiExpAmount = ofNonNegative(x.amount + y.amount)

    override def compare(x: SeichiExpAmount, y: SeichiExpAmount): Int = x.amount.compareTo(y.amount)
  }

  implicit lazy val lowerBounded: LowerBounded[SeichiExpAmount] = new LowerBounded[SeichiExpAmount] {
    override val partialOrder: PartialOrder[SeichiExpAmount] = orderedMonus
    override val minBound: SeichiExpAmount = zero
  }
  //endregion
}

