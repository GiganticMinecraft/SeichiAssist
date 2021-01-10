package com.github.unchama.seichiassist.temprepo

import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class GiganticBerserkRateLimitValue(amount: Int) extends AnyVal {
  def add(value: GiganticBerserkRateLimitValue) =
    GiganticBerserkRateLimitValue(this.amount + value)
}

private abstract class GiganticBerserkValueInstances {
  implicit lazy val ordering: Ordering[GiganticBerserkRateLimitValue] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[GiganticBerserkRateLimitValue] = {
    HasMinimum.as(GiganticBerserkRateLimitValue.ofNonNegative(0))
  }

  implicit lazy val orderedMonus: OrderedMonus[GiganticBerserkRateLimitValue] = {
    new OrderedMonus[GiganticBerserkRateLimitValue] {
      override def compare(x: GiganticBerserkRateLimitValue, y: GiganticBerserkRateLimitValue): Int =
        x.amount.compare(y.amount)

      override val empty: GiganticBerserkRateLimitValue =
        GiganticBerserkRateLimitValue.ofNonNegative(0)

      override def combine(x: GiganticBerserkRateLimitValue, y: GiganticBerserkRateLimitValue): GiganticBerserkRateLimitValue =
        GiganticBerserkRateLimitValue.ofNonNegative(x.amount + y.amount)

      override def |-|(x: GiganticBerserkRateLimitValue, y: GiganticBerserkRateLimitValue): GiganticBerserkRateLimitValue = GiganticBerserkRateLimitValue.ofNonNegative {
        (x.amount - y.amount) max 0
      }
    }
  }
}

object GiganticBerserkRateLimitValue extends GiganticBerserkValueInstances {
  def ofNonNegative(amount: Int): GiganticBerserkRateLimitValue = {
    require(
      amount >= 0,
      "amount must not negative"
    )

    GiganticBerserkRateLimitValue(amount)
  }
}
