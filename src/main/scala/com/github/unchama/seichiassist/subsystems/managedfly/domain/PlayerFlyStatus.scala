package com.github.unchama.seichiassist.subsystems.managedfly.domain

sealed trait PlayerFlyStatus {
  val tickOneMinute: PlayerFlyStatus
}

case class Flying(remainingDuration: RemainingFlyDuration) extends PlayerFlyStatus {
  override lazy val tickOneMinute: PlayerFlyStatus = {
    remainingDuration.tickOneMinute match {
      case Some(value) => Flying(value)
      case None => NotFlying
    }
  }
}

case object NotFlying extends PlayerFlyStatus {
  override val tickOneMinute: PlayerFlyStatus = this
}
