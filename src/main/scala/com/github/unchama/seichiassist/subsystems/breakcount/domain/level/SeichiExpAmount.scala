package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import cats.Order
import cats.kernel.{CommutativeMonoid, LowerBounded, PartialOrder}

/**
 * 整地量を表す値のクラス。非負の値に対応する。
 */
case class SeichiExpAmount private(amount: Long) extends AnyVal {

  def mapAmount(f: Long => Long): SeichiExpAmount =
    SeichiExpAmount.ofNonNegative(f(amount))

  def add(a: SeichiExpAmount): SeichiExpAmount = mapAmount(_ + a.amount)

}

private[level] abstract class SeichiExpAmountInstances {

  import cats.implicits._

  implicit lazy val order: Order[SeichiExpAmount] = Order.by(_.amount)

  lazy val zero: SeichiExpAmount = SeichiExpAmount.ofNonNegative(0)

  implicit lazy val lowerBounded: LowerBounded[SeichiExpAmount] = new LowerBounded[SeichiExpAmount] {
    override val partialOrder: PartialOrder[SeichiExpAmount] = order
    override val minBound: SeichiExpAmount = zero
  }

  implicit lazy val addition: CommutativeMonoid[SeichiExpAmount] =
    CommutativeMonoid.instance(zero, (a, b) => SeichiExpAmount.ofNonNegative(a.amount + b.amount))
}

object SeichiExpAmount extends SeichiExpAmountInstances {
  def ofNonNegative(amount: Long): SeichiExpAmount = {
    require(
      amount >= 0L,
      "整地経験値量は非負である必要があります。"
    )

    SeichiExpAmount(amount)
  }
}

