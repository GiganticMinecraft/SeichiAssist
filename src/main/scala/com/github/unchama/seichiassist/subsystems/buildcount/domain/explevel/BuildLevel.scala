package com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel

import cats.{Eq, Order}
import com.github.unchama.generic.algebra.typeclasses.PositiveInt

case class BuildLevel private (level: Int) extends AnyVal {
  def incremented: BuildLevel = BuildLevel(level + 1)
}

private[explevel] abstract class SeichiLevelInstances {

  import cats.implicits._

  implicit val positiveInt: PositiveInt[BuildLevel] = new PositiveInt[BuildLevel] {
    override def wrapPositive(int: Int): BuildLevel = {
      require(int >= 1)
      BuildLevel(int)
    }

    override def asInt(t: BuildLevel): Int = t.level
  }

  implicit val eq: Eq[BuildLevel] = Eq.by(_.level)

  implicit val order: Order[BuildLevel] = Order.by(_.level)
}

object BuildLevel extends SeichiLevelInstances {

  def ofPositive(rawLevel: Int): BuildLevel = positiveInt.wrapPositive(rawLevel)

}
