package com.github.unchama.seichiassist.subsystems.managedfly.domain

import cats.kernel.Semigroup

sealed trait RemainingFlyDuration {

  /**
   * 「1分」減らせるなら減らしたものを、無理ならNoneを返す
   *
   * @return
   */
  val tickOneMinute: Option[RemainingFlyDuration]

}

object RemainingFlyDuration {

  case object Infinity extends RemainingFlyDuration {
    override val tickOneMinute: Option[RemainingFlyDuration] = Some(this)
  }

  case class PositiveMinutes private(value: Int) extends RemainingFlyDuration {
    override lazy val tickOneMinute: Option[RemainingFlyDuration] = {
      import PositiveMinutes.fromPositive

      value match {
        case 1 => None
        case _ => Some(fromPositive(value - 1))
      }
    }
  }

  object PositiveMinutes {
    def fromPositive(unsafeValue: Int): PositiveMinutes = {
      require(unsafeValue > 0)

      PositiveMinutes(unsafeValue)
    }
  }

  implicit val remainingFlyDurationSemigroup: Semigroup[RemainingFlyDuration] = {
    import PositiveMinutes.fromPositive

    (x: RemainingFlyDuration, y: RemainingFlyDuration) =>
      (x, y) match {
        case (Infinity, _) => Infinity
        case (_, Infinity) => Infinity
        case (PositiveMinutes(nx), PositiveMinutes(ny)) =>
          val sum = nx.toLong + ny.toLong
          if (sum <= Int.MaxValue)
            fromPositive(sum.toInt)
          else
            Infinity
      }
  }
}
