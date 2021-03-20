package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.effect.concurrent.Deferred
import cats.effect.{Async, Concurrent, ConcurrentEffect, Effect, Fiber, Sync, SyncEffect, Timer}
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{PrefetchResult, SinglePhasedRepositoryInitialization}
import com.github.unchama.fs2.workaround.Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.Mutex
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList

object EffectListRepositoryDefinitions {

  import cats.implicits._

  import scala.concurrent.duration._

  /**
   * [[FastDiggingEffectList]] と、それを1秒ごとにトピックへ通知するファイバーの組。
   */
  type RepositoryValue[F[_], G[_]] = (Mutex[F, G, FastDiggingEffectList], Deferred[F, Fiber[F, Nothing]])

  def initialization[
    F[_] : Concurrent,
    G[_] : Sync : ContextCoercion[*[_], F]
  ]: SinglePhasedRepositoryInitialization[G, RepositoryValue[F, G]] =
    (_, _) => {
      for {
        ref <- Mutex.of[F, G, FastDiggingEffectList](FastDiggingEffectList.empty)
        deferred <- Deferred.in[G, F, Fiber[F, Nothing]]
      } yield PrefetchResult.Success(ref, deferred)
    }

  def tappingAction[
    F[_] : ConcurrentEffect : Timer,
    G[_] : SyncEffect : ContextCoercion[*[_], F],
    Player
  ](effectTopic: Topic[F, Option[(Player, FastDiggingEffectList)]]): (Player, RepositoryValue[F, G]) => G[Unit] =
    (player, value) => {
      val (mutexRef, fiberPromise) = value

      val programToRun: F[Unit] = for {
        publishingEffectFiber <-
          Concurrent[F].start[Nothing] {
            fs2.Stream
              .awakeEvery[F](1.second)
              .evalMap[F, FastDiggingEffectList](_ => ContextCoercion(mutexRef.readLatest))
              .evalTap[F, Unit](effectList => effectTopic.publish1(Some(player, effectList)))
              .compile.drain
              .flatMap[Nothing](_ => Async[F].never[Nothing])
          }
        _ <- fiberPromise.complete(publishingEffectFiber)
      } yield ()

      EffectExtra.runAsyncAndForget[F, G, Unit](programToRun)
    }

  def finalization[
    F[_] : Effect,
    G[_] : SyncEffect,
    Player
  ]: RepositoryFinalization[G, Player, RepositoryValue[F, G]] =
    RepositoryFinalization.withoutAnyPersistence[G, Player, RepositoryValue[F, G]] { (_, value) =>
      val (_, fiberPromise) = value

      EffectExtra.runAsyncAndForget[F, G, Unit] {
        for {
          fiber <- fiberPromise.get
          _ <- fiber.cancel
        } yield ()
      }
    }
}
