package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.repository

import cats.Applicative
import cats.effect.concurrent.{Deferred, Ref}
import cats.effect.{Async, Concurrent, ConcurrentEffect, Effect, Fiber, Sync}
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.effect.FastDiggingEffectList
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.{EffectListDiff, FastDiggingEffectStatsSettings, FastDiggingEffectStatsSettingsPersistence}
import fs2.concurrent.Topic
import io.chrisdavenport.cats.effect.time.JavaTime

import java.util.UUID

object EffectStatsSettingsRepository {

  /**
   * [[FastDiggingEffectStatsSettings]] と、それをトピックに60秒に一度通知するプロセスの組
   */
  type RepositoryValue[F[_], G[_]] = (Ref[G, FastDiggingEffectStatsSettings], Deferred[F, Fiber[F, Nothing]])

  def defaultValue[G[_] : Applicative]: G[FastDiggingEffectStatsSettings] = Applicative[G].pure {
    FastDiggingEffectStatsSettings.AlwaysReceiveDetails
  }

  import cats.implicits._

  def initialization[
    F[_] : Concurrent,
    G[_] : Sync : ContextCoercion[*[_], F]
  ](persistence: FastDiggingEffectStatsSettingsPersistence[G]): SinglePhasedRepositoryInitialization[G, RepositoryValue[F, G]] = {
    SinglePhasedRepositoryInitialization.forRefCell {
      RefDictBackedRepositoryInitialization.usingUuidRefDict(persistence)(defaultValue[G])
    }.extendPreparation { (_, _) => ref => Deferred.in[G, F, Fiber[F, Nothing]].map(ref -> _) }
  }

  def tappingAction[
    F[_] : ConcurrentEffect : JavaTime,
    G[_] : Sync : ContextCoercion[*[_], F],
    Player: HasUuid
  ](topic: Topic[F, Option[(Player, (EffectListDiff, FastDiggingEffectStatsSettings))]],
    effectClock: fs2.Stream[F, (Player, FastDiggingEffectList)]): (Player, RepositoryValue[F, G]) => G[Unit] =
    (player, pair) => {
      val (ref, fiberPromise) = pair

      val processStream: fs2.Stream[F, Unit] = {
        effectClock
          .through(StreamExtra.valuesWithKeyOfSameUuidAs(player))
          .through(StreamExtra.takeEvery(60))
          .evalMap { list => list.filteredList }
          .map(_.map(_.effect))
          .sliding(2)
          .mapFilter { queue =>
            queue.lastOption.map { latest =>
              val previous = queue.dropRight(1).lastOption

              EffectListDiff(previous, latest)
            }
          }
          .evalMap { diff => ContextCoercion(ref.get.map(diff -> _)) }
          .evalTap { pair => topic.publish1(Some(player, pair)) }
          .as(())
      }

      EffectExtra.runAsyncAndForget[F, G, Unit] {
        Concurrent[F]
          .start[Nothing](processStream.compile.drain.flatMap[Nothing](_ => Async[F].never))
          .flatMap(fiberPromise.complete)
      }
    }

  def finalization[
    F[_] : Effect, G[_] : Sync, Player: HasUuid
  ](persistence: FastDiggingEffectStatsSettingsPersistence[G]): RepositoryFinalization[G, UUID, RepositoryValue[F, G]] = {
    RepositoryFinalization.liftToRefFinalization[G, UUID, FastDiggingEffectStatsSettings] {
      RefDictBackedRepositoryFinalization.using(persistence)(identity)
    }.withIntermediateEffects[RepositoryValue[F, G]] {
      case (ref, _) =>
        Applicative[G].pure(ref)
    } {
      case (ref, fiber) =>
        // 終了時にファイバーの開始を待ち、開始されたものをすぐにcancelする
        EffectExtra
          .runAsyncAndForget[F, G, Unit] {
            fiber.get.flatMap(_.cancel)
          }
          .as(ref)
    }
  }
}
