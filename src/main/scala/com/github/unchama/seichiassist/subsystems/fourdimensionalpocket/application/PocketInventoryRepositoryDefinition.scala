package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.application

import cats.effect.{Async, Fiber, Sync}
import cats.effect.std.Dispatcher
import com.github.unchama.datarepository.definitions.{
  FiberAdjoinedRepositoryDefinition,
  MutexRepositoryDefinition,
  RefDictBackedRepositoryDefinition
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.effect.EffectExtra
import com.github.unchama.generic.effect.concurrent.Mutex
import com.github.unchama.generic.effect.stream.StreamExtra
import com.github.unchama.generic.{ContextCoercion, Diff}
import com.github.unchama.minecraft.algebra.HasUuid
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiLevel
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.{
  CreateInventory,
  InteractInventory
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.{
  PocketInventoryPersistence,
  PocketSizeTable
}
import org.typelevel.log4cats.ErrorLogger
import cats.effect.Deferred

object PocketInventoryRepositoryDefinition {

  import cats.effect.syntax.all._
  import cats.implicits._

  /**
   * プレーヤーのポケットインベントリと、それを整地レベルに応じて更新するプロセスの組
   */
  type RepositoryValue[F[_], G[_], Inventory] =
    (Mutex[F, G, Inventory], Deferred[F, Fiber[F, Throwable, Nothing]])

  def withContext[
    F[_]: Async: Dispatcher: ErrorLogger,
    G[_]: Sync: [f[_]] =>> ContextCoercion[f, F],
    Player: HasUuid,
    Inventory: [a] =>> CreateInventory[G, a]: [a] =>> InteractInventory[F, Player, a]
  ](
    persistence: PocketInventoryPersistence[G, Inventory],
    levelStream: fs2.Stream[F, (Player, Diff[SeichiLevel])]
  ): RepositoryDefinition[G, Player, RepositoryValue[F, G, Inventory]] =
    FiberAdjoinedRepositoryDefinition
      .extending {
        MutexRepositoryDefinition.over[F, G, Player, Inventory] {
          RefDictBackedRepositoryDefinition.usingUuidRefDictWithEffectfulDefault(persistence) {
            CreateInventory[G, Inventory].create(PocketSizeTable.default)
          }
        }
      }
      .withAnotherTappingAction { (player, pair) =>
        {
          val (ref, fiberPromise) = pair

          val processStream: fs2.Stream[F, Unit] = {
            levelStream.through(StreamExtra.valuesWithKeyOfSameUuidAs(player)).evalMap {
              case Diff(_, right) =>
                val newSize = PocketSizeTable(right)
                val update: Inventory => F[Inventory] =
                  InteractInventory[F, Player, Inventory].extendSize(newSize)(_)

                ref.lockAndUpdate(update).as(())
            }
          }

          EffectExtra.runAsyncAndForget[F, G, Unit] {
            StreamExtra
              .compileToRestartingStream[F, Nothing]("[PocketInventoryRepository]")(
                processStream
              )
              .start
              .flatMap(fiber => fiberPromise.complete(fiber).void)
          }
        }
      }
}
