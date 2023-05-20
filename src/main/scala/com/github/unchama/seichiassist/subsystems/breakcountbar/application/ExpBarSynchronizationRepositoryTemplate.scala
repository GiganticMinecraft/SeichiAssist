package com.github.unchama.seichiassist.subsystems.breakcountbar.application

import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, Sync}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.BreakCountBarVisibility
import io.chrisdavenport.log4cats.ErrorLogger

object ExpBarSynchronizationRepositoryTemplate {

  type BossBarWithPlayer[F[_], P] = MinecraftBossBar[F] { type Player = P }

  /**
   * レポジトリが保持する値の型。
   *
   * 一つ目の成分にプレーヤーが持つ整地量ボスバー、 二つ目の成分にボスバーを可視設定と同期するためのファイバーへの参照を持つ。
   *
   * ファイバーへの参照は、プレーヤーがサーバーに参加しているほとんどのタイミングにおいて すでにcompleteされていることが期待される。
   * このようなデザインになっているのは、[[F]] とは異なる文脈でレポジトリのデータを初期化する必要があり、 `Fiber[F, Unit]` が `G`
   * のコンテキストで入手できない可能性があるからである。
   */
  type RepositoryValueType[F[_], P] =
    (BossBarWithPlayer[F, P], Deferred[F, Fiber[F, Unit]])

  import cats.effect.implicits._
  import cats.implicits._

  def initialization[G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[
    G,
    *[_]
  ]: ErrorLogger, Player: HasUuid](
    breakCountReadAPI: BreakCountReadAPI[F, G, Player],
    visibilityValues: fs2.Stream[F, (Player, BreakCountBarVisibility)]
  )(
    createFreshBossBar: G[BossBarWithPlayer[F, Player]]
  ): TwoPhasedRepositoryInitialization[G, Player, RepositoryValueType[F, Player]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching { player =>
      for {
        bossBar <- createFreshBossBar

        synchronization = fs2
          .Stream
          .eval(breakCountReadAPI.seichiAmountDataRepository(player).read)
          .translate(ContextCoercion.asFunctionK[G, F])
          .append(
            breakCountReadAPI
              .seichiAmountUpdates
              .through(StreamExtra.valuesWithKeyOfSameUuidAs(player))
          )
          .evalTap(BreakCountBarManipulation.write(_, bossBar))

        switching = visibilityValues
          .through(StreamExtra.valuesWithKeyOfSameUuidAs(player))
          .evalTap(v => bossBar.visibility.write(BreakCountBarVisibility.Shown == v))

        fiberPromise <- Deferred.in[G, F, Fiber[F, Unit]]

        _ <- EffectExtra.runAsyncAndForget[F, G, Unit](bossBar.players.add(player))
        _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
          StreamExtra
            .compileToRestartingStream[F, Unit]("[ExpBarSynchronizationRepositoryTemplate]") {
              switching.concurrently(synchronization)
            }
            .start >>= fiberPromise.complete
        }
      } yield (bossBar, fiberPromise)
    }

  def finalization[G[_]: Sync, F[_]: ConcurrentEffect: ContextCoercion[G, *[_]], Player]
    : RepositoryFinalization[G, Player, RepositoryValueType[F, Player]] =
    RepositoryFinalization.withoutAnyPersistence {
      case (_, (_, fiberPromise)) =>
        EffectExtra.runAsyncAndForget[F, G, Unit](fiberPromise.get.flatMap(_.cancel))
    }
}
