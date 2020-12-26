package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait PresentClaimingState
object PresentClaimingState {
  case object NotClaimed extends PresentClaimingState
  case object Claimed extends PresentClaimingState
  case object Unavailable extends PresentClaimingState
}

