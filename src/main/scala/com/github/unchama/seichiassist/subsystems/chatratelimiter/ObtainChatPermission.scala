package com.github.unchama.seichiassist.subsystems.chatratelimiter

import cats.Monad
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ratelimiting.RateLimiter
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.{ChatCount, ChatPermissionRequestResult}
import org.bukkit.entity.Player

trait ObtainChatPermission[F[_], Player] {
  def tryPermitted(player: Player): F[ChatPermissionRequestResult]
}

object ObtainChatPermission {
  import cats.implicits._

  def from[G[_] : Monad](
                          repository: PlayerDataRepository[Ref[G, Option[RateLimiter[G, ChatCount]]]]
                        ): ObtainChatPermission[G, Player] =
    player => for {
      rateLimiterOpt <- repository.apply(player).get
      folded <- rateLimiterOpt.fold(
        Monad[G].pure[ChatPermissionRequestResult](ChatPermissionRequestResult.Success)
      ) { rateLimiter =>
        rateLimiter.requestPermission(ChatCount.One).map(count =>
          if (count == ChatCount.One) ChatPermissionRequestResult.Success
          else ChatPermissionRequestResult.Failed)
      }
    } yield folded
}