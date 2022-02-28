package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait PresentClaimingState
object PresentClaimingState {

  /**
   * まだ受け取っていないことを示す
   */
  case object NotClaimed extends PresentClaimingState

  /**
   * すでに受け取っていることを示す
   */
  case object Claimed extends PresentClaimingState

  /**
   * 対象になっていないなど、受け取ることができないことを示す
   */
  case object Unavailable extends PresentClaimingState
}
