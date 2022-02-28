package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait GrantRejectReason

object GrantRejectReason {
  case object NoSuchPresentID extends GrantRejectReason
}
