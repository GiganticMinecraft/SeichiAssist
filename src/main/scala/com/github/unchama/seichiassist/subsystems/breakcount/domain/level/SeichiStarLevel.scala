package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import cats.Order
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiStarLevel.ofNonNegative

/**
 * 整地スターレベル。
 */
case class SeichiStarLevel private (level: BigInt) extends AnyVal {
  def increment: SeichiStarLevel = ofNonNegative(level + 1)
}

private[level] abstract class SeichiStarLevelInstances {

  import cats.implicits._

  implicit lazy val order: Order[SeichiStarLevel] = Order.by(_.level)

}

object SeichiStarLevel extends SeichiStarLevelInstances {

  def ofNonNegative(n: BigInt): SeichiStarLevel = {
    require(n >= 0, "整地スターレベルは非負である必要があります。")
    SeichiStarLevel(n)
  }

  val zero: SeichiStarLevel = ofNonNegative(0)

}
