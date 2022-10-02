package com.github.unchama.seichiassist.subsystems.gacha.domain

sealed trait GachaEventOperationResult

object GachaEventOperationResult {

  case object Success extends GachaEventOperationResult

  case object Fail extends GachaEventOperationResult

}
