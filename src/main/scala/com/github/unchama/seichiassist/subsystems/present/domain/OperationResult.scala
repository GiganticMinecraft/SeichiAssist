package com.github.unchama.seichiassist.subsystems.present.domain

object OperationResult {
  sealed trait DeleteResult
  object DeleteResult {
    case object Done extends DeleteResult
    case object NotFount extends DeleteResult
  }
}
