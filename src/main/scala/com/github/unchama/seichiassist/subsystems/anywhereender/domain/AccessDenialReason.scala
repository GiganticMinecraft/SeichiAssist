package com.github.unchama.seichiassist.subsystems.anywhereender.domain

import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

sealed trait AccessDenialReason
object AccessDenialReason {
  import SeichiLevel._
  import cats.implicits._

  case class NotEnoughLevel(current: SeichiLevel, required: SeichiLevel)
      extends AccessDenialReason {
    require(current < required)
  }
}
