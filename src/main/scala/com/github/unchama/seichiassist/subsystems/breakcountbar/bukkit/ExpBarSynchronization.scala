package com.github.unchama.seichiassist.subsystems.breakcountbar.bukkit

import cats.Applicative
import cats.effect.concurrent.Ref
import cats.effect.{Async, ConcurrentEffect, Fiber, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.TwoPhasedPlayerDataRepository
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.bukkit.objects.BukkitBossBar
import com.github.unchama.minecraft.objects.MinecraftBossBar
import com.github.unchama.seichiassist.subsystems.breakcount.domain.SeichiAmountData
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{BreakCountBarManipulation, BreakCountBarVisibility}
import org.bukkit.boss.{BarColor, BarStyle}
import org.bukkit.entity.{Player => BukkitPlayer}

import java.util.UUID

class ExpBarSynchronization[
  F[_] : ConcurrentEffect,
  G[_] : SyncEffect : ContextCoercion[*[_], F]
](breakCountValues: fs2.Stream[F, (BukkitPlayer, SeichiAmountData)],
  visibilityValues: fs2.Stream[F, (BukkitPlayer, BreakCountBarVisibility)])
  extends TwoPhasedPlayerDataRepository[G, (MinecraftBossBar[F] {type Player = BukkitPlayer}, Ref[G, Fiber[F, Unit]])] {

  import cats.effect.implicits._
  import cats.implicits._

  override protected type TemporaryData = Unit
  override protected val loadTemporaryData: (String, UUID) => G[Either[Option[String], Unit]] =
    (_, _) => Applicative[G].pure(Right(()))

  override protected def initializeValue(player: BukkitPlayer, temporaryData: Unit): G[(MinecraftBossBar[F] {
    type Player = BukkitPlayer
  }, Ref[G, Fiber[F, Unit]])] =
    for {
      bossBar <- BukkitBossBar.in[G, F]("", BarColor.YELLOW, BarStyle.SOLID)

      synchronization = StreamExtra
        .filterKeys(breakCountValues, player)
        .evalTap(BreakCountBarManipulation.write(_, bossBar))

      switching = StreamExtra
        .filterKeys(visibilityValues, player)
        .evalTap(v => bossBar.visibility.write(BreakCountBarVisibility.Shown == v))

      ref <- Ref.of[G, Fiber[F, Unit]](Fiber(Async[F].never, Async[F].unit))
      _ <- EffectExtra.runAsyncAndForget[F, G, Unit] {
        switching.concurrently(synchronization)
          .compile
          .drain
          .start
          .flatMap(fiber => ContextCoercion(ref.set(fiber)))
      }
    } yield (bossBar, ref)

  override protected val finalizeBeforeUnload: (Any, (Any, Ref[G, Fiber[F, Unit]])) => G[Unit] =
    (_, pair) => pair._2.get.map(_.cancel)
}
