package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait RevokeWarning

object RevokeWarning {
  case object NoSuchPresentID extends RevokeWarning
  case object NoPlayers extends RevokeWarning
}
