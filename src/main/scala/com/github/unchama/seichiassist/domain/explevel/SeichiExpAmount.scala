package com.github.unchama.seichiassist.domain.explevel

import com.github.unchama.seichiassist.util.typeclass.HasMinimum

case class SeichiExpAmount private(amount: BigInt) extends AnyVal {
  /**
   * @deprecated 後方互換性用。整地量はすべてBigIntであるべき。
   */
  @Deprecated def toInt: Int = amount.toInt

  /**
   * @deprecated 後方互換性用。整地量はすべてBigIntであるべき。
   */
  @Deprecated def toLong: Long = amount.toLong
}

private[explevel] abstract class SeichiExpAmountInstances {
  implicit lazy val ordering: Ordering[SeichiExpAmount] = Ordering.by(_.amount)

  implicit lazy val hasMinimum: HasMinimum[SeichiExpAmount] = {
    HasMinimum.as(SeichiExpAmount.ofNonNegative(0))
  }
}

object SeichiExpAmount extends SeichiExpAmountInstances {
  def ofNonNegative(amount: BigInt): SeichiExpAmount = {
    require(amount >= 0)
    SeichiExpAmount(amount)
  }

  def ofNonNegative(amount: Long): SeichiExpAmount = {
    require(amount >= 0)
    SeichiExpAmount(amount)
  }

  def ofNonNegative(amount: Int): SeichiExpAmount = {
    require(amount >= 0)
    SeichiExpAmount(amount)
  }
}
