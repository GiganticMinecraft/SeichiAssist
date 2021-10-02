package com.github.unchama.seichiassist.subsystems.chatratelimiter

import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.ChatPermissionRequestResult

trait InspectChatRateLimit[F[_], Player] {
  def tryPermitted(player: Player): F[ChatPermissionRequestResult]
}
