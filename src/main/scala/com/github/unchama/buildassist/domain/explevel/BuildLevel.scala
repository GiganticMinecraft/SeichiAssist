package com.github.unchama.buildassist.domain.explevel

import com.github.unchama.seichiassist.domain.explevel.Level

case class BuildLevel private(level: Int) extends AnyVal

private[explevel] abstract class SeichiLevelInstances {
  implicit val level: Level[BuildLevel] = (rawLevel: Int) => {
    require(rawLevel >= 1)
    BuildLevel(rawLevel)
  }
}

object BuildLevel extends SeichiLevelInstances {

  def ofPositive(rawLevel: Int): BuildLevel = level.wrap(rawLevel)

}
