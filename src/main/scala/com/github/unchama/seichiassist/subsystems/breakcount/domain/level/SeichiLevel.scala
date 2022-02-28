package com.github.unchama.seichiassist.subsystems.breakcount.domain.level

import cats.Order
import com.github.unchama.generic.algebra.typeclasses.{HasSuccessor, PositiveInt}

/**
 * 整地レベル。正の[[Int]]と対応する。
 */
case class SeichiLevel private (level: Int) extends AnyVal {
  def increment: SeichiLevel = SeichiLevel.ofPositive(level + 1)
}

private[level] abstract class SeichiLevelInstances {

  import cats.implicits._

  implicit lazy val positiveInt: PositiveInt[SeichiLevel] = new PositiveInt[SeichiLevel] {
    override def wrapPositive(rawLevel: Int): SeichiLevel = {
      require(rawLevel >= 1)
      SeichiLevel(rawLevel)
    }

    override def asInt(t: SeichiLevel): Int = t.level
  }

  implicit lazy val order: Order[SeichiLevel] = Order.by(_.level)

  implicit lazy val hasSuccessor: HasSuccessor[SeichiLevel] =
    HasSuccessor.positiveIntHasSuccessor
}

object SeichiLevel extends SeichiLevelInstances {

  def ofPositive(rawLevel: Int): SeichiLevel = positiveInt.wrapPositive(rawLevel)

}
