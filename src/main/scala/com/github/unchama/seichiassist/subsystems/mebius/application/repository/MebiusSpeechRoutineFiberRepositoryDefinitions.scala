package com.github.unchama.seichiassist.subsystems.mebius.application.repository

import cats.effect.{Async, Fiber, IO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.mebius.bukkit.routines.PeriodicMebiusSpeechRoutine
import com.github.unchama.seichiassist.subsystems.mebius.service.MebiusSpeechService
import org.bukkit.entity.Player
import cats.effect.Deferred
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.dispatcher

object MebiusSpeechRoutineFiberRepositoryDefinitions {

  type RepositoryValue[F[_]] = Deferred[F, Fiber[F, Throwable, Nothing]]

  import cats.implicits._

  // TODO PeriodicMebiusSpeechRoutineはbukkitに依存しているため、抽象化層をもう一層置くべき
  def initialization[G[_]: Sync](
    implicit serviceRepository: KeyedDataRepository[Player, MebiusSpeechService[SyncIO]],
    repeatingTaskContext: RepeatingTaskContext,
    onMainThread: OnMinecraftServerThread[IO],
    ioConcurrent: Async[IO]
  ): TwoPhasedRepositoryInitialization[G, Player, RepositoryValue[IO]] =
    TwoPhasedRepositoryInitialization.withoutPrefetching[G, Player, RepositoryValue[IO]] {
      player =>
        for {
          promise <- Deferred.in[G, IO, Fiber[IO, Throwable, Nothing]]
          _ <- EffectExtra.runAsyncAndForget[IO, G, Unit] {
            PeriodicMebiusSpeechRoutine
              .start(player)
              .evalOn(repeatingTaskContext)
              .start
              .flatMap(fiber => promise.complete(fiber).void)
          }
        } yield promise
    }

  def finalization[G[_]: Sync, Player]: RepositoryFinalization[G, Player, RepositoryValue[IO]] =
    RepositoryFinalization.withoutAnyPersistence[G, Player, RepositoryValue[IO]] {
      (_, promise) =>
        EffectExtra.runAsyncAndForget[IO, G, Unit] {
          promise.get.flatMap(_.cancel)
        }
    }

}
