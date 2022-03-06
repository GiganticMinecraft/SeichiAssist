package com.github.unchama.seichiassist.subsystems.chatratelimiter.domain

sealed trait ChatPermissionRequestResult

object ChatPermissionRequestResult {
  case object Success extends ChatPermissionRequestResult
  case object Failed extends ChatPermissionRequestResult
}
