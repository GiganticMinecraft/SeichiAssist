package com.github.unchama.seichiassist.subsystems.present.domain

sealed trait PaginationRejectReason

object PaginationRejectReason {
  case object Empty extends PaginationRejectReason
  case class TooLargePage(exceptedMax: Long) extends PaginationRejectReason
}
