package com.github.unchama.seichiassist.subsystems.managedfly.domain

import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration.Minutes.fromNonNegative

sealed trait RemainingFlyDuration {

  def tickOneMinute: Option[RemainingFlyDuration]

  def hasExpired: Boolean = tickOneMinute.isEmpty

}

object RemainingFlyDuration {

  case object Infinity extends RemainingFlyDuration {
    override def tickOneMinute: Option[RemainingFlyDuration] = Some(this)
  }

  case class Minutes private(value: Int) extends RemainingFlyDuration {
    override def tickOneMinute: Option[RemainingFlyDuration] = {
      value match {
        case 0 => None
        case _ => Some(fromNonNegative(value - 1))
      }
    }
  }

  object Minutes {
    def fromNonNegative(unsafeValue: Int): Minutes = {
      require(unsafeValue >= 0)

      Minutes(unsafeValue)
    }
  }

}
