package com.github.unchama.seichiassist.subsystems.managedfly.domain

sealed trait PlayerFlyStatus

case class Flying(remainingDuration: RemainingFlyDuration) extends PlayerFlyStatus

case object NotFlying extends PlayerFlyStatus

object PlayerFlyStatus {
  def toDurationOption(status: PlayerFlyStatus): Option[RemainingFlyDuration] = status match {
    case Flying(remainingDuration) => Some(remainingDuration)
    case NotFlying => None
  }
}
