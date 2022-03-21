package com.github.unchama.seichiassist.subsystems.chatratelimiter

import cats.effect.{Concurrent, ConcurrentEffect, SyncEffect, Timer}
import cats.implicits._
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.chatratelimiter.bukkit.listeners.RateLimitCheckListener
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.{
  ChatRateLimitRepositoryDefinition,
  ObtainChatPermission
}
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.entity.Player
import org.bukkit.event.Listener

object System {
  def wired[F[_]: ConcurrentEffect: ErrorLogger, G[_]: SyncEffect: ContextCoercion[
    *[_],
    F
  ]: Timer](implicit breakCountAPI: BreakCountReadAPI[F, G, Player]): F[Subsystem[F]] = {
    val repository = ChatRateLimitRepositoryDefinition.withContext[F, G, Player]
    for {
      handle <- ContextCoercion(BukkitRepositoryControls.createHandles(repository))
      _ <- Concurrent[F].start[Nothing]( // NOTE: This explicit type argument is needed
        StreamExtra
          .compileToRestartingStream("チャットのレートリミット")(breakCountAPI.seichiLevelUpdates.evalTap {
            case (p, _) =>
              ContextCoercion(handle.repository(p).set(None))
          })
      )
    } yield {
      new Subsystem[F] {
        implicit val api: ObtainChatPermission[G, Player] =
          ObtainChatPermission.from(handle.repository)

        override val listeners: Seq[Listener] =
          Seq(new RateLimitCheckListener)

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(handle.coerceFinalizationContextTo[F])
      }
    }
  }
}
