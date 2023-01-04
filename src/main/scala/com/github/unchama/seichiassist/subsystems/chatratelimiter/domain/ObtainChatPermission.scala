package com.github.unchama.seichiassist.subsystems.chatratelimiter.domain

import cats.Monad
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.generic.ratelimiting.RateLimiter
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel

class ObtainChatPermission[F[_], G[_]: Monad, Player](
  rateLimiterRepository: KeyedDataRepository[Player, RateLimiter[G, ChatCount]]
)(implicit breakCountReadAPI: BreakCountReadAPI[F, G, Player]) {
  import cats.implicits._

  def forPlayer(player: Player): G[ChatPermissionRequestResult] =
    for {
      playerLevel <- breakCountReadAPI.seichiLevelRepository(player).read
      result <-
        if (playerLevel > SeichiLevel(1)) {
          Monad[G].pure(ChatPermissionRequestResult.Success)
        } else {
          rateLimiterRepository(player).requestPermission(ChatCount.One).map {
            case ChatCount.One  => ChatPermissionRequestResult.Success
            case ChatCount.Zero => ChatPermissionRequestResult.Failed
          }
        }
    } yield result
}
