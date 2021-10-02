package com.github.unchama.seichiassist.subsystems.chatratelimiter

import cats.Monad
import cats.effect.{Concurrent, ConcurrentEffect, SyncEffect, Timer}
import cats.effect.implicits._
import cats.implicits._
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.{ContextCoercion, Diff}
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.chatratelimiter.bukkit.listeners.RateLimitCheckListener
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.{ChatCount, ChatPermissionRequestResult, ChatRateLimitRepositoryDefinition}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait System[F[_], G[_], Player] extends Subsystem[F] {
  implicit val api: InspectChatRateLimit[G, Player]
}

object System {
  def wired[
    F[_] : ConcurrentEffect : Timer : ErrorLogger,
    G[_] : SyncEffect : ContextCoercion[*[_], F] : Monad,
  ](implicit breakCountAPI: BreakCountAPI[F, G, Player]): F[System[F, G, Player]] = {
    val repository = ChatRateLimitRepositoryDefinition.withContext[F, G, Player]
    for {
      handle <- ContextCoercion(BukkitRepositoryControls.createHandles(repository))
      _ <- Concurrent[F].start[Nothing]( // NOTE: This explicit type argument is needed
        StreamExtra.compileToRestartingStream("チャットのレートリミット")(
          breakCountAPI.seichiLevelUpdates.evalTap { case (p, _) =>
            ContextCoercion(handle.repository(p).set(None))
          }
        )
      )
    } yield {
      new System[F, G, Player] {
        override val listeners: Seq[Listener] =
          Seq(new RateLimitCheckListener)

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(handle.coerceFinalizationContextTo[F])

        override implicit val api: InspectChatRateLimit[G, Player] = (player: Player) => {
          for {
            rateLimiterOpt <- handle.repository(player).get
            folded <- rateLimiterOpt.fold(Monad[G].pure[ChatPermissionRequestResult](ChatPermissionRequestResult.Success)) { rateLimiter =>
              val canPermittedG = rateLimiter.requestPermission(ChatCount(1)).map(_ == ChatCount(1))
              canPermittedG.map(canPermitted =>
                if (canPermitted) ChatPermissionRequestResult.Success
                else ChatPermissionRequestResult.Failed)
            }
          } yield folded
        }
      }
    }
  }
}
