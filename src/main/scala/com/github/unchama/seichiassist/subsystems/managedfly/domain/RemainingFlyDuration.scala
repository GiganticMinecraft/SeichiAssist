package com.github.unchama.seichiassist.subsystems.managedfly.domain

import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration.PositiveMinutes.fromPositive

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

}
