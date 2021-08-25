package com.github.unchama.seichiassist.subsystems.subhome.domain

object OperationResult {
  sealed trait RenameResult
  object RenameResult {
    case object Done extends RenameResult
    case object NotFound extends RenameResult
  }
}
