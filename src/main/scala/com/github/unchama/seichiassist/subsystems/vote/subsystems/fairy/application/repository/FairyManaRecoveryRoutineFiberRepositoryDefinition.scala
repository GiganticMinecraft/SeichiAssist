package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.effect.{Async, Fiber, IO, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine
import cats.effect.Deferred
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.dispatcher

object FairyManaRecoveryRoutineFiberRepositoryDefinition {

  def initialization[Player](fairyRoutine: FairyRoutine[IO, Player])(
    implicit context: RepeatingTaskContext,
    concurrentEffect: Async[IO]
  ): TwoPhasedRepositoryInitialization[
    SyncIO,
    Player,
    Deferred[IO, Fiber[IO, Throwable, Nothing]]
  ] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[SyncIO, Player, Deferred[IO, Fiber[IO, Throwable, Nothing]]] {
        player =>
          for {
            promise <- Deferred.in[SyncIO, IO, Fiber[IO, Throwable, Nothing]]
            _ <-
              EffectExtra.runAsyncAndForget[IO, SyncIO, Unit] {
                fairyRoutine
                  .start(player)
                  .evalOn(context)
                  .start
                  .flatMap(fiber => promise.complete(fiber).void)
              }
          } yield promise
      }

  def finalization[Player]
    : RepositoryFinalization[SyncIO, Player, Deferred[IO, Fiber[IO, Throwable, Nothing]]] =
    RepositoryFinalization
      .withoutAnyPersistence[SyncIO, Player, Deferred[IO, Fiber[IO, Throwable, Nothing]]] {
        (_, promise) =>
          EffectExtra.runAsyncAndForget[IO, SyncIO, Unit] {
            promise.get.flatMap(_.cancel)
          }
      }

}
