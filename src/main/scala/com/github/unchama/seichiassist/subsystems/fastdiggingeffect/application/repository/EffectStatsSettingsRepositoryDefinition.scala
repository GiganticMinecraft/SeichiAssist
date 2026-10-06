package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.effect.{Async, Fiber, Sync}
import cats.effect.std.Dispatcher
import com.github.unchama.datarepository.definitions.{
  FiberAdjoinedRepositoryDefinition,
  RefDictBackedRepositoryDefinition
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.{
  EffectListDiff,
  FastDiggingEffectStatsSettings,
  FastDiggingEffectStatsSettingsPersistence
}
import fs2.Pipe
import org.typelevel.log4cats.ErrorLogger
import cats.effect.{Deferred, Ref}

object EffectStatsSettingsRepositoryDefinition {

  /**
   * [[FastDiggingEffectStatsSettings]] と、それをトピックに60秒に一度通知するプロセスの組
   */
  type RepositoryValue[F[_], G[_]] =
    (Ref[G, FastDiggingEffectStatsSettings], Deferred[F, Fiber[F, Throwable, Nothing]])

  import cats.effect.syntax.all._
  import cats.implicits._

  def withContext[F[_]: Async: Dispatcher: ErrorLogger, G[_]: Sync: [f[_]] =>> ContextCoercion[
    f,
    F
  ], Player: HasUuid](
    persistence: FastDiggingEffectStatsSettingsPersistence[G],
    publishEffectDiff: Pipe[
      F,
      (Player, (EffectListDiff, FastDiggingEffectStatsSettings)),
      Unit
    ],
    effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)]
  ): RepositoryDefinition[G, Player, RepositoryValue[F, G]] = {
    FiberAdjoinedRepositoryDefinition
      .extending {
        RefDictBackedRepositoryDefinition
          .usingUuidRefDict[G, Player, FastDiggingEffectStatsSettings](persistence)(
            FastDiggingEffectStatsSettings.AlwaysReceiveDetails
          )
          .toRefRepository
      }
      .withAnotherTappingAction {
        case (player, pair) =>
          val (ref, fiberPromise) = pair

          val processStream: fs2.Stream[F, Unit] = {
            effectClock
              .through(StreamExtra.valuesWithKeyOfSameUuidAs(player))
              .through(StreamExtra.takeEvery(60))
              .evalMap { list => list.filteredList[F] }
              .map(_.map(_.effect))
              .sliding(2)
              .mapFilter { queue =>
                queue.toList.lastOption.map { latest =>
                  val previous = queue.toList.dropRight(1).lastOption

                  EffectListDiff(previous, latest)
                }
              }
              .evalMap { diff => ContextCoercion(ref.get.map(diff -> _)) }
              .map(player -> _)
              .through(publishEffectDiff)
          }

          EffectExtra.runAsyncAndForget[F, G, Unit] {
            StreamExtra
              .compileToRestartingStream[F, Nothing]("[EffectStatsSettingsRepository]") {
                processStream
              }
              .start >>= (fiberPromise.complete(_).void)
          }
      }
  }
}
