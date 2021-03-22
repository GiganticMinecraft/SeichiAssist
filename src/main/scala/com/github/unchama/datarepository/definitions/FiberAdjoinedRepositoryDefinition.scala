package com.github.unchama.datarepository.definitions

import cats.Monad
import cats.effect.concurrent.Deferred
import cats.effect.{ConcurrentEffect, Fiber, Sync}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.effect.EffectExtra

object FiberAdjoinedRepositoryDefinition {

  import cats.implicits._

  def extending[
    G[_] : Sync,
    F[_] : ConcurrentEffect,
    Player, R
  ](definition: RepositoryDefinition[G, Player, R]): definition.Self[(R, Deferred[F, Fiber[F, Nothing]])] =
    definition
      .flatXmapWithIntermediateEffects(r =>
        Deferred
          .in[G, F, Fiber[F, Nothing]]
          .map(r -> _)
      ) { case (r, _) =>
        Monad[G].pure(r)
      } { case (r, fiber) =>
        // 終了時にファイバーの開始を待ち、開始されたものをすぐにcancelする
        EffectExtra
          .runAsyncAndForget[F, G, Unit] {
            fiber.get.flatMap(_.cancel)
          }
          .as(r)
      }
}
