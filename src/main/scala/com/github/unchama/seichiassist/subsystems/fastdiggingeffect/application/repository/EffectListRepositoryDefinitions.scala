package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.effect.std.Dispatcher
import cats.effect.{Async, Deferred, Fiber, Sync}
import com.github.unchama.datarepository.definitions.FiberAdjoinedRepositoryDefinition.FiberAdjoined
import com.github.unchama.datarepository.template.finalization.RepositoryFinalization
import com.github.unchama.datarepository.template.initialization.{
  PrefetchResult,
  SinglePhasedRepositoryInitialization
}
import com.github.unchama.fs2.workaround.fs3.Fs3Topic
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.Mutex
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList
import org.typelevel.log4cats.ErrorLogger

object EffectListRepositoryDefinitions {

  import cats.effect.syntax.all._
  import cats.implicits._

  import scala.concurrent.duration._

  /**
   * [[FastDiggingEffectList]] と、それを1秒ごとにトピックへ通知するファイバーの組。
   */
  type RepositoryValue[F[_], G[_]] = Mutex[F, G, FastDiggingEffectList] FiberAdjoined F

  def initialization[F[_]: Async, G[_]: Sync: [f[_]] =>> ContextCoercion[f, F]]
    : SinglePhasedRepositoryInitialization[G, RepositoryValue[F, G]] =
    (_, _) => {
      for {
        ref <- Mutex.of[F, G, FastDiggingEffectList](FastDiggingEffectList.empty)
        deferred <- Deferred.in[G, F, Fiber[F, Throwable, Nothing]]
      } yield PrefetchResult.Success(ref, deferred)
    }

  def tappingAction[F[_]: Async: Dispatcher: ErrorLogger, G[_]: Sync: [f[
    _
  ]] =>> ContextCoercion[f, F], Player](
    effectTopic: Fs3Topic[F, Option[(Player, FastDiggingEffectList)]]
  ): (Player, RepositoryValue[F, G]) => G[Unit] = {
    case (player, (mutexRef, fiberPromise)) =>
      val programToRun: F[Unit] =
        StreamExtra
          .compileToRestartingStream[F, Nothing]("[EffectListRepositoryDefinitions]") {
            fs2.Stream.fixedRate[F](1.second).evalMap { _ =>
              ContextCoercion(mutexRef.readLatest).flatMap { latestEffectList =>
                effectTopic.publish1(Some(player, latestEffectList))
              }
            }
          }
          .start
          .flatMap(fiber => fiberPromise.complete(fiber).void)

      EffectExtra.runAsyncAndForget[F, G, Unit](programToRun)
  }

  def finalization[F[_]: Async: Dispatcher, G[_]: Sync, Player]
    : RepositoryFinalization[G, Player, RepositoryValue[F, G]] =
    RepositoryFinalization.withoutAnyPersistence[G, Player, RepositoryValue[F, G]] {
      (_, value) =>
        val (_, fiberPromise) = value

        EffectExtra.runAsyncAndForget[F, G, Unit] {
          for {
            fiber <- fiberPromise.get
            _ <- fiber.cancel
          } yield ()
        }
    }
}
