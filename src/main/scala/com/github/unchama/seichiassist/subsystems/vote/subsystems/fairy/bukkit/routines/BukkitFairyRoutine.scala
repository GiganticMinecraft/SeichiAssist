package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.routines

import cats.effect.{ConcurrentEffect, IO, Sync, SyncEffect, SyncIO, Timer}
import com.github.unchama.concurrent.{RepeatingRoutine, RepeatingTaskContext}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.bukkit.actions.BukkitRecoveryMana
import org.bukkit.entity.Player

import scala.concurrent.duration.FiniteDuration

class BukkitFairyRoutine[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[*[_], F]]
    extends FairyRoutine[F, G, Player] {

  override def start(player: Player)(
    implicit breakCountAPI: BreakCountAPI[F, G, Player],
    fairyAPI: FairyAPI[F, G, Player],
    voteAPI: VoteAPI[F, Player],
    manaApi: ManaApi[F, G, Player],
    context: RepeatingTaskContext
  ): F[Nothing] = {

    val repeatInterval: IO[FiniteDuration] = IO {
      import scala.concurrent.duration._

      1.minute
    }

    implicit val timer: Timer[IO] = IO.timer(context)

    implicit val onMainThread: OnMinecraftServerThread[IO] =
      PluginExecutionContexts.onMainThread

    Sync[F].delay {
      RepeatingRoutine
        .permanentRoutine(
          repeatInterval,
          onMainThread.runAction {
            SyncIO {
              BukkitRecoveryMana[F, G](player).recovery
            }
          }
        )
        .unsafeRunSync()
    }
  }

}
