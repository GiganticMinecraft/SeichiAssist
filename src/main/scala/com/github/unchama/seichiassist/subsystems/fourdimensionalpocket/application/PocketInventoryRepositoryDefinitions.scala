package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.application

import cats.effect.concurrent.Deferred
import cats.effect.{Async, Concurrent, ConcurrentEffect, Effect, Fiber, Sync}
import com.github.unchama.datarepository.template.{RefDictBackedRepositoryFinalization, RefDictBackedRepositoryInitialization, RepositoryFinalization, SinglePhasedRepositoryInitialization}
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.Mutex
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.generic.{ContextCoercion, Diff}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.{CreateInventory, InteractInventory}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.{PocketInventoryPersistence, PocketSizeTable}

import java.util.UUID

object PocketInventoryRepositoryDefinitions {

  import cats.implicits._

  // TODO EffectStatsSettingsRepositoryとほぼ同型なので再利用ができないか？

  /**
   * プレーヤーのポケットインベントリと、それを整地レベルに応じて更新するプロセスの組
   */
  type RepositoryValue[F[_], G[_], Inventory] = (Mutex[F, G, Inventory], Deferred[F, Fiber[F, Nothing]])

  def initialization[
    F[_] : ConcurrentEffect,
    G[_] : Sync : ContextCoercion[*[_], F],
    Player,
    Inventory: CreateInventory[G, *]
  ](persistence: PocketInventoryPersistence[G, Inventory])
  : SinglePhasedRepositoryInitialization[G, RepositoryValue[F, G, Inventory]] = {
    RefDictBackedRepositoryInitialization
      .usingUuidRefDict(persistence) {
        CreateInventory.apply.create(PocketSizeTable.default)
      }
      .extendPreparation { (_, _) =>
        inventory =>
          for {
            mutex <- Mutex.of[F, G, Inventory](inventory)
            promise <- Deferred.in[G, F, Fiber[F, Nothing]]
          } yield (mutex, promise)
      }
  }

  def tappingAction[
    F[_] : ConcurrentEffect,
    G[_] : Sync : ContextCoercion[*[_], F],
    Player: HasUuid,
    Inventory: InteractInventory[F, Player, *]
  ](levelStream: fs2.Stream[F, (Player, Diff[SeichiLevel])]): (Player, RepositoryValue[F, G, Inventory]) => G[Unit] =
    (player, pair) => {
      val uuid = HasUuid[Player].of(player)

      val (ref, fiberPromise) = pair

      val processStream: fs2.Stream[F, Unit] =
        StreamExtra
          .valuesWithKeyFilter(levelStream)(HasUuid[Player].of(_) == uuid)
          .evalMap { case Diff(_, right) =>
            val newSize = PocketSizeTable(right)
            val update: Inventory => F[Inventory] =
              InteractInventory[F, Player, Inventory].extendSize(newSize)(_)

            ref.lockAndUpdate(update).as(())
          }

      EffectExtra.runAsyncAndForget[F, G, Unit] {
        Concurrent[F]
          .start[Nothing](processStream.compile.drain.flatMap[Nothing](_ => Async[F].never))
          .flatMap(fiberPromise.complete)
      }
    }

  def finalization[
    F[_] : Effect, G[_] : Sync, Inventory
  ](persistence: PocketInventoryPersistence[G, Inventory])
  : RepositoryFinalization[G, UUID, RepositoryValue[F, G, Inventory]] = {
    RefDictBackedRepositoryFinalization
      .using(persistence)(identity[UUID])
      .withIntermediateEffects[RepositoryValue[F, G, Inventory]] {
        case (ref, _) => ref.readLatest
      } {
        case (ref, fiber) =>
          // 終了時にファイバーの開始を待ち、開始されたものをすぐにcancelする
          EffectExtra
            .runAsyncAndForget[F, G, Unit] {
              fiber.get.flatMap(_.cancel)
            } >> ref.readLatest
      }
  }
}
