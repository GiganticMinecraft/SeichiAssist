package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.repository

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, IO, Sync, SyncIO}
import com.github.unchama.concurrent.RepeatingTaskContext
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.TwoPhasedRepositoryInitialization
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountAPI
import com.github.unchama.seichiassist.subsystems.mana.ManaApi
import com.github.unchama.seichiassist.subsystems.vote.VoteAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.FairyAPI
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.application.actions.FairyRoutine

object FairyManaRecoveryRoutineFiberRepositoryDefinition {

  import cats.implicits._

  implicit val ioCE: ConcurrentEffect[IO] =
    IO.ioConcurrentEffect(PluginExecutionContexts.asyncShift)

  def initialization[F[_]: Sync, Player](
    implicit fairyRoutine: FairyRoutine[IO, SyncIO, Player],
    fairyAPI: FairyAPI[IO, Player],
    breakCountAPI: BreakCountAPI[IO, SyncIO, Player],
    voteAPI: VoteAPI[IO, Player],
    manaApi: ManaApi[IO, SyncIO, Player],
    context: RepeatingTaskContext
  ): TwoPhasedRepositoryInitialization[F, Player, Deferred[IO, Fiber[IO, Nothing]]] =
    TwoPhasedRepositoryInitialization
      .withoutPrefetching[F, Player, Deferred[IO, Fiber[IO, Nothing]]] { player =>
        for {
          promise <- Deferred.in[F, IO, Fiber[IO, Nothing]]
          _ <- EffectExtra.runAsyncAndForget[IO, F, Unit] {
            fairyRoutine
              .start(player)
              .toIO
              .start(IO.contextShift(context))
              .flatMap(fiber => promise.complete(fiber))
          }
        } yield promise
      }

  def finalization[F[_]: Sync, Player]
    : RepositoryFinalization[F, Player, Deferred[IO, Fiber[IO, Nothing]]] =
    RepositoryFinalization.withoutAnyPersistence[F, Player, Deferred[IO, Fiber[IO, Nothing]]] {
      (_, promise) =>
        EffectExtra.runAsyncAndForget[IO, F, Unit] {
          promise.get.flatMap(_.cancel)
        }
    }

}
