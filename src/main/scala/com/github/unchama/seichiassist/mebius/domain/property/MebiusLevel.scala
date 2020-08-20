package com.github.unchama.seichiassist.mebius.domain.property

import cats.effect.IO

import scala.util.Random

case class MebiusLevel private(value: Int) extends AnyVal {

  def attemptLevelUp: IO[Boolean] =
    if (isMaximum) {
      IO.pure(false)
    } else IO {
      // パラメータpの幾何分布の平均は1/pであるから、
      // 1ブロック壊すごとに 1 / averageAttemptsToLevelUp の確率でレベルアップが起これば
      // 平均 averageAttemptsToLevelUp 回の試行でレベルアップすることになる。
      Random.nextInt(MebiusLevel.averageAttemptsToLevelUp(value - 1)) == 0
    }

  def isMaximum: Boolean = value == MebiusLevel.max

  def increment: MebiusLevel = MebiusLevel(value + 1)
}

object MebiusLevel {

  implicit val mebiusLevelOrder: Ordering[MebiusLevel] = Ordering.by(_.value)

  // TODO should be wrapped
  val max = 30

  private val averageAttemptsToLevelUp = List(
    500, 500, 500, 500, // 5
    800, 800, 800, 800, 800, // 10
    1700, 1700, 1700, 1700, 1700, // 15
    1800, 1800, 1800, 1800, 1800, // 20
    2200, 2200, 2200, 2200, 2200, // 25
    2600, 2600, 2600, 2600, 3000 // 30
  )

  def apply(level: Int): MebiusLevel = {
    require(1 <= level && level <= max)

    new MebiusLevel(level)
  }

}
