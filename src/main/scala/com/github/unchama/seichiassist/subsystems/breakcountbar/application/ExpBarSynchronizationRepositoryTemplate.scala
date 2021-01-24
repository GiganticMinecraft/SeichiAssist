package com.github.unchama.seichiassist.subsystems.breakcountbar.application

import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, Sync}
import com.github.unchama.datarepository.template.{RepositoryFinalization, TwoPhasedRepositoryInitialization}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.BreakCountBarVisibility

object ExpBarSynchronizationRepositoryTemplate {

  type BossBarWithPlayer[F[_], P] = MinecraftBossBar[F] {type Player = P}

  type RepositoryValueType[G[_], F[_], P] =
    (BossBarWithPlayer[F, P], Deferred[F, Fiber[F, Unit]])

  import cats.effect.implicits._
  import cats.implicits._

  def initialization[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Player,
  ](breakCountValues: fs2.Stream[F, (Player, SeichiAmountData)],
    visibilityValues: fs2.Stream[F, (Player, BreakCountBarVisibility)])
   (createFreshBossBar: G[BossBarWithPlayer[F, Player]])
  : TwoPhasedRepositoryInitialization[G, Player, RepositoryValueType[G, F, Player]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching { player =>
      for {
        bossBar <- createFreshBossBar

        synchronization = StreamExtra
          .filterKeys(breakCountValues, player)
          .evalTap(BreakCountBarManipulation.write(_, bossBar))

        switching = StreamExtra
          .filterKeys(visibilityValues, player)
          .evalTap(v => bossBar.visibility.write(BreakCountBarVisibility.Shown == v))

        fiberPromise <- Deferred.in[G, F, Fiber[F, Unit]]

        _ <- EffectExtra.runAsyncAndForget[F, G, Unit](bossBar.players.add(player))
        _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
          switching.concurrently(synchronization)
            .compile
            .drain
            .start
            .flatMap(fiberPromise.complete)
        }
      } yield (bossBar, fiberPromise)
    }

  def finalization[
    G[_] : Sync,
    F[_] : ConcurrentEffect : ContextCoercion[G, *[_]],
    Player,
  ]: RepositoryFinalization[G, Player, RepositoryValueType[G, F, Player]] =
    RepositoryFinalization.withoutAnyPersistence { case (_, (_, fiberPromise)) =>
      EffectExtra.runAsyncAndForget[F, G, Unit](fiberPromise.get.flatMap(_.cancel))
    }
}
