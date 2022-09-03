package com.github.unchama.seichiassist.subsystems.awayscreenname.application.repository

import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, IO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.subsystems.awayscreenname.bukkit.routines.BukkitPlayerAwayTimeRecalculationRoutine
import com.github.unchama.seichiassist.subsystems.awayscreenname.domain.{PlayerIdleMinuteRepository, PlayerLocationRepository}

object PlayerAwayTimeRecalculationRoutineFiberRepositoryDefinitions {

  type RepositoryValue[F[_]] = Deferred[F, Fiber[F, Nothing]]

  def initialization[F[_]: Sync, Player, Location](implicit
                                         locationRepository: KeyedDataRepository[
                                           Player,
                                           PlayerLocationRepository[SyncIO, Location, Player]
                                         ],
                                         idleMinuteRepository: KeyedDataRepository[Player, PlayerIdleMinuteRepository[SyncIO]],
                                         repeatingTaskContext: RepeatingTaskContext,
                                         onMainThread: OnMinecraftServerThread[IO],
                                         ioConcurrent: ConcurrentEffect[IO])
    : TwoPhasedRepositoryInitialization[F, Player, RepositoryValue[IO]] = { player =>
    for {
      promise <- Deferred.in[F, IO, Fiber[IO, Nothing]]
      _ <- EffectExtra.runAsyncAndForget[IO, F, Unit] {
        BukkitPlayerAwayTimeRecalculationRoutine
          .start(player)
          .start(IO.contextShift(repeatingTaskContext))
      }
    }
  }

}
