package com.github.unchama.seichiassist.subsystems.chatratelimiter

import cats.effect.{Async, Sync}
import cats.implicits._
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.{ContextCoercion, UnsafeSyncRunner}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.chatratelimiter.application.ChatRateLimitRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.chatratelimiter.bukkit.listeners.RateLimitCheckListener
import com.github.unchama.seichiassist.subsystems.chatratelimiter.domain.ObtainChatPermission
import org.bukkit.entity.Player
import org.bukkit.event.Listener

object System {
  def wired[F[_]: Async, G[_]: Sync: UnsafeSyncRunner: [f[_]] =>> ContextCoercion[f, F]: [g[
    _
  ]] =>> ContextCoercion[g, cats.effect.SyncIO]](
    implicit breakCountAPI: BreakCountReadAPI[F, G, Player]
  ): F[Subsystem[F]] = {
    val repository = ChatRateLimitRepositoryDefinition.inSyncContext[G, Player]
    val toF = ContextCoercion.asFunctionK[G, F]

    for {
      handle <- toF(BukkitRepositoryControls.createHandles(repository))
    } yield {
      implicit val api: ObtainChatPermission[F, G, Player] =
        new ObtainChatPermission(handle.repository)

      new Subsystem[F] {
        override val listeners: Seq[Listener] =
          Seq(new RateLimitCheckListener[F, G])

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(handle.coerceFinalizationContextTo[F])
      }
    }
  }
}
