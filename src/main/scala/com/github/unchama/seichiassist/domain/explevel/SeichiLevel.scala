package com.github.unchama.seichiassist.domain.explevel

case class SeichiLevel private(level: Int) extends AnyVal

private abstract class SeichiLevelInstances {
  implicit val level: Level[SeichiLevel] = (rawLevel: Int) => {
    require(rawLevel >= 1)
    SeichiLevel(rawLevel)
  }
}

object SeichiLevel extends SeichiLevelInstances {

  def ofPositive(rawLevel: Int): SeichiLevel = level.wrap(rawLevel)

}
