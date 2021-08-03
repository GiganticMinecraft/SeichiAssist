package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait PaginationRejectReason

object PaginationRejectReason {
  case class TooLargePage(exceptedMax: Long) extends PaginationRejectReason
}
