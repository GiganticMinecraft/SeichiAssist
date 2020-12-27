package com.github.unchama.buildassist.domain.explevel

import cats.{Eq, Order}
import com.github.unchama.seichiassist.domain.explevel.Level

case class BuildLevel private(level: Int) extends AnyVal

private[explevel] abstract class SeichiLevelInstances {

  import cats.implicits._

  implicit val level: Level[BuildLevel] = (rawLevel: Int) => {
    require(rawLevel >= 1)
    BuildLevel(rawLevel)
  }

  implicit val eq: Eq[BuildLevel] = Eq.by(_.level)

  implicit val order: Order[BuildLevel] = Order.by(_.level)
}

object BuildLevel extends SeichiLevelInstances {

  def ofPositive(rawLevel: Int): BuildLevel = level.wrapPositive(rawLevel)

}
