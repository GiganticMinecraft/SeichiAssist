package com.github.unchama.seichiassist.mebius.domain.property

import cats.effect.Sync

import scala.util.Random

case class MebiusLevel private(value: Int) extends AnyVal {

  def attemptLevelUp[F[_]](implicit F: Sync[F]): F[Boolean] =
    if (isMaximum) F.pure(false)
    else F.delay {
      // パラメータpの幾何分布の平均は1/pであるから、
      // 1ブロック壊すごとに 1 / averageAttemptsToLevelUp の確率でレベルアップが起これば
      // 平均 averageAttemptsToLevelUp 回の試行でレベルアップすることになる。
      Random.nextInt(MebiusLevel.averageAttemptsToLevelUp(value - 1)) == 0
    }

  def isMaximum: Boolean = this == MebiusLevel.max

  def increment: Option[MebiusLevel] =
    if (isMaximum)
      None
    else
      Some(MebiusLevel(value + 1))
}

object MebiusLevel {

  implicit val mebiusLevelOrder: Ordering[MebiusLevel] = Ordering.by(_.value)

  val max: MebiusLevel = new MebiusLevel(30)

  private val averageAttemptsToLevelUp = List(
    500, 500, 500, 500, // 5
    800, 800, 800, 800, 800, // 10
    1700, 1700, 1700, 1700, 1700, // 15
    1800, 1800, 1800, 1800, 1800, // 20
    2200, 2200, 2200, 2200, 2200, // 25
    2600, 2600, 2600, 2600, 3000 // 30
  )

  def apply(level: Int): MebiusLevel = {
    require(1 <= level && level <= max.value)

    new MebiusLevel(level)
  }

}
