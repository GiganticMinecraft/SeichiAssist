package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import cats.Order

/**
 * 整地スターレベル。
 */
case class SeichiStarLevel private(level: Int) extends AnyVal

private[level] abstract class SeichiStarLevelInstances {

  import cats.implicits._

  implicit lazy val order: Order[SeichiStarLevel] = Order.by(_.level)

}

object SeichiStarLevel extends SeichiStarLevelInstances {

  def ofNonNegative(n: Int): SeichiStarLevel = {
    require(n >= 0, "整地スターレベルは非負である必要があります。")
    SeichiStarLevel(n)
  }

}
