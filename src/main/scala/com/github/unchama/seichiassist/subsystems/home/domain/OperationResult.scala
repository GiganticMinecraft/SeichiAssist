package com.github.unchama.seichiassist.subsystems.home.domain

object OperationResult {
  sealed trait RenameResult
  object RenameResult {
    case object Done extends RenameResult
    case object NotFound extends RenameResult
  }
}
