package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait PresentClaimingState {
  val label: String
}

object PresentClaimingState {
  /**
   * 受け取り対象ではあるが、まだ受け取っていないことを示す
   */
  case object NotClaimed extends PresentClaimingState {
    override val label: String = "受け取り可能"
  }

  /**
   * 受け取り対象で、すでに受け取っていることを示す
   */
  case object Claimed extends PresentClaimingState {
    override val label: String = "受け取り済み"
  }

  /**
   * 受け取り対象ではないことを示す
   */
  case object Unavailable extends PresentClaimingState {
    override val label: String = "配布対象外"
  }
}
