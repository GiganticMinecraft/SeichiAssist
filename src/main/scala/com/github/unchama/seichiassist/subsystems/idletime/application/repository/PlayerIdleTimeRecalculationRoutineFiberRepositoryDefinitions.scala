package com.github.unchama.seichiassist.subsystems.idletime.application.repository

import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, IO, Sync}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.idletime.domain.PlayerIdleTimeRecalculationRoutine

object PlayerIdleTimeRecalculationRoutineFiberRepositoryDefinitions {

  import cats.implicits._

  type RepositoryValue[F[_]] = Deferred[F, Fiber[F, Nothing]]

  def initialization[F[_]: Sync, Player](
    playerIdleTimeRecalculationRoutine: Player => PlayerIdleTimeRecalculationRoutine[Player]
  )(
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    concurrentEffect: ConcurrentEffect[IO]
  ): TwoPhasedRepositoryInitialization[F, Player, RepositoryValue[IO]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, RepositoryValue[IO]] {
      player =>
        for {
          promise <- Deferred.in[F, IO, Fiber[IO, Nothing]]
          _ <- EffectExtra.runAsyncAndForget[IO, F, Unit] {
            playerIdleTimeRecalculationRoutine(player)
              .start
              .start(IO.contextShift(repeatingTaskContext))
              .flatMap(fiber => promise.complete(fiber))
          }
        } yield promise
    }

  def finalization[F[_]: Sync, Player]: RepositoryFinalization[F, Player, RepositoryValue[IO]] =
    RepositoryFinalization.withoutAnyPersistence[F, Player, RepositoryValue[IO]] {
      (_, promise) =>
        EffectExtra.runAsyncAndForget[IO, F, Unit] {
          promise.get.flatMap(_.cancel)
        }
    }

}
