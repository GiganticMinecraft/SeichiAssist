package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait PresentClaimingState

object PresentClaimingState {

  /**
   * 受け取り対象ではあるが、まだ受け取っていないことを示す
   */
  case object NotClaimed extends PresentClaimingState

  /**
   * 受け取り対象で、すでに受け取っていることを示す
   */
  case object Claimed extends PresentClaimingState

  /**
   * 受け取り対象ではないことを示す
   */
  case object Unavailable extends PresentClaimingState
}
