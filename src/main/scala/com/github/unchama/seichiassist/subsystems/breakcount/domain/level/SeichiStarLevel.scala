package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import com.github.unchama.generic.algebra.typeclasses.PositiveInt

/**
 * 整地スターレベル。
 */
case class SeichiStarLevel private(level: Int) extends AnyVal

private[domain] abstract class SeichiStarLevelInstances {

  implicit lazy val positiveInt: PositiveInt[SeichiStarLevel] = new PositiveInt[SeichiStarLevel] {
    override def wrapPositive(int: Int): SeichiStarLevel = {
      require(int >= 1)
      SeichiStarLevel(int)
    }

    override def asInt(t: SeichiStarLevel): Int = t.level
  }

}

object SeichiStarLevel extends SeichiStarLevelInstances
