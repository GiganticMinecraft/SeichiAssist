package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application

import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{Async, Concurrent, ConcurrentEffect, Effect, Fiber, Sync, SyncEffect, Timer}
import com.github.unchama.datarepository.template.{PrefetchResult, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList
import fs2.concurrent.Topic

object EffectListRepositoryDefinitions {

  import cats.implicits._

  import scala.concurrent.duration._

  /**
   * [[FastDiggingEffectList]] と、それを1秒ごとにトピックへ通知するファイバーの組。
   */
  type RepositoryValue[F[_]] = (Ref[F, FastDiggingEffectList], Deferred[F, Fiber[F, Nothing]])

  def initialization[F[_] : Concurrent, G[_] : Sync]: SinglePhasedRepositoryInitialization[G, RepositoryValue[F]] =
    (_, _) => {
      for {
        ref <- Ref.in[G, F, FastDiggingEffectList](FastDiggingEffectList.empty)
        deferred <- Deferred.in[G, F, Fiber[F, Nothing]]
      } yield PrefetchResult.Success(ref, deferred)
    }

  def tappingAction[
    F[_] : ConcurrentEffect : Timer,
    G[_] : SyncEffect,
    Player
  ](effectTopic: Topic[F, (Player, FastDiggingEffectList)]): (Player, RepositoryValue[F]) => G[Unit] =
    (player, value) => {
      val (ref, fiberPromise) = value

      val programToRun: F[Unit] = for {
        publishingEffectFiber <-
          Concurrent[F].start[Nothing] {
            fs2.Stream
              .awakeEvery[F](1.second)
              .evalMap(_ => ref.get)
              .evalTap(effectList => effectTopic.publish1(player, effectList))
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
  ]: RepositoryFinalization[G, Player, RepositoryValue[F]] =
    RepositoryFinalization.withoutAnyPersistence[G, Player, RepositoryValue[F]] { (_, value) =>
      val (_, fiberPromise) = value

      EffectExtra.runAsyncAndForget[F, G, Unit] {
        for {
          fiber <- fiberPromise.get
          _ <- fiber.cancel
        } yield ()
      }
    }
}
