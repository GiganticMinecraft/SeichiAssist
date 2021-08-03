package com.github.unchama.seichiassist.subsystems.present.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.Positive

sealed trait PaginationRejectReason

object PaginationRejectReason {
  case class TooLargePage(exceptedMax: Int Refined Positive) extends PaginationRejectReason
}