package com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.application.repository

import cats.effect.{Async, Fiber, IO, Sync}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.idletime.subsystems.awayscreenname.domain.{
  PlayerScreenNameUpdateRoutine,
  UpdatePlayerScreenName
}
import cats.effect.Deferred
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.dispatcher

object PlayerScreenNameUpdateRoutineFiberRepositoryDefinitions {

  import cats.implicits._

  type RepositoryValue[F[_]] = Deferred[F, Fiber[F, Throwable, Nothing]]

  def initialization[F[_]: Sync, Player](
    playerScreenNameUpdateRoutine: PlayerScreenNameUpdateRoutine[Player]
  )(
    implicit repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    concurrentEffect: Async[IO],
    updatePlayerScreenName: UpdatePlayerScreenName[IO, Player]
  ): TwoPhasedRepositoryInitialization[F, Player, RepositoryValue[IO]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[F, Player, RepositoryValue[IO]] {
      player =>
        for {
          promise <- Deferred.in[F, IO, Fiber[IO, Throwable, Nothing]]
          _ <- EffectExtra.runAsyncAndForget[IO, F, Unit] {
            playerScreenNameUpdateRoutine
              .start(player)
              .evalOn(repeatingTaskContext)
              .start
              .flatMap(fiber => promise.complete(fiber).void)
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
