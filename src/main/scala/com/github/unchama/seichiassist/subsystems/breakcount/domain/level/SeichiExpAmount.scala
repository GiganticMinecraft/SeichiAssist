package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import cats.kernel.CommutativeMonoid
import com.github.unchama.seichiassist.util.typeclass.HasMinimum

/**
 * 整地量を表す値のクラス。非負の値に対応する。
 */
case class SeichiExpAmount private(amount: Long) extends AnyVal {

  def mapAmount(f: Long => Long): SeichiExpAmount =
    SeichiExpAmount.ofNonNegative(f(amount))

  def add(a: SeichiExpAmount): SeichiExpAmount = mapAmount(_ + a.amount)

}

private[explevel] abstract class SeichiExpAmountInstances {
  implicit lazy val ordering: Ordering[SeichiExpAmount] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[SeichiExpAmount] = {
    HasMinimum.as(SeichiExpAmount.ofNonNegative(0))
  }

  implicit lazy val addition: CommutativeMonoid[SeichiExpAmount] =
    CommutativeMonoid.instance(hasMinimum.minimum, (a, b) => SeichiExpAmount.ofNonNegative(a.amount + b.amount))
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

