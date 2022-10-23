package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, IO, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine

object FairyManaRecoveryRoutineFiberRepositoryDefinition {

  def initialization[Player](fairyRoutine: FairyRoutine[IO, Player])(
    implicit context: RepeatingTaskContext,
    concurrentEffect: ConcurrentEffect[IO]
  ): TwoPhasedRepositoryInitialization[SyncIO, Player, Deferred[IO, Fiber[IO, Nothing]]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[SyncIO, Player, Deferred[IO, Fiber[IO, Nothing]]] { player =>
        for {
          promise <- Deferred.in[SyncIO, IO, Fiber[IO, Nothing]]
          _ <-
            EffectExtra.runAsyncAndForget[IO, SyncIO, Unit] {
              fairyRoutine
                .start(player)
                .start(IO.contextShift(context))
                .flatMap(fiber => promise.complete(fiber))
            }
        } yield promise
      }

  def finalization[Player]
    : RepositoryFinalization[SyncIO, Player, Deferred[IO, Fiber[IO, Nothing]]] =
    RepositoryFinalization
      .withoutAnyPersistence[SyncIO, Player, Deferred[IO, Fiber[IO, Nothing]]] { (_, promise) =>
        EffectExtra.runAsyncAndForget[IO, SyncIO, Unit] {
          promise.get.flatMap(_.cancel)
        }
      }

}
