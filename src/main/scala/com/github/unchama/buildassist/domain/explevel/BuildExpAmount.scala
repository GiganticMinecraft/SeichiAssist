package com.github.unchama.buildassist.domain.explevel

import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class BuildExpAmount private(amount: BigDecimal) extends AnyVal {

  // TODO 1/2ブロックのカウント等を許すために有理数型を使うべき(RateLimiterがそれらに対応するべき)
  def floor: Int = {
    val bigInt = amount.toBigInt
    if (bigInt > BigInt(Int.MaxValue)) Int.MaxValue else bigInt.intValue
  }

  def incrementBy(n: Int): BuildExpAmount = BuildExpAmount.ofNonNegative(amount + n)

  def toPlainString: String = amount.setScale(1, BigDecimal.RoundingMode.HALF_UP).bigDecimal.toPlainString

  def mapAmount(f: BigDecimal => BigDecimal): BuildExpAmount = BuildExpAmount.ofNonNegative(f(amount))

}

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

  def ofNonNegative(amount: Int): BuildExpAmount = ofNonNegative(BigDecimal(amount))
}
