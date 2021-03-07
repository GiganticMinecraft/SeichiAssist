package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.bungeesemaphoreresponder.domain.PlayerDataFinalizer
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.application.PocketInventoryRepositoryDefinitions
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.{CreateInventory, InteractInventory}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.{PocketInventoryPersistence, PocketSize}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.infrastructure.{CreateBukkitInventory, InteractBukkitInventory, JdbcBukkitPocketInventoryPersistence}
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory

trait System[F[_], Player] extends Subsystem[F] {

  val api: FourDimensionalPocketApi[F, Player]

}

object System {

  import cats.implicits._
  import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid._

  def wired[
    F[_] : ConcurrentEffect,
    G[_] : SyncEffect : ContextCoercion[*[_], F],
    H[_]
  ](breakCountReadAPI: BreakCountReadAPI[F, G, Player]): F[System[F, Player]] = {
    val persistence: PocketInventoryPersistence[G, Inventory] =
      new JdbcBukkitPocketInventoryPersistence[G]

    implicit val createInventory: CreateInventory[G, Inventory] =
      new CreateBukkitInventory[G]

    implicit val interactInventory: InteractInventory[F, Player, Inventory] =
      new InteractBukkitInventory[F]

    for {
      pocketInventoryRepositoryHandles <-
        ContextCoercion {
          BukkitRepositoryControls
            .createTappingSinglePhasedRepositoryAndHandles(
              PocketInventoryRepositoryDefinitions.initialization(persistence),
              PocketInventoryRepositoryDefinitions.tappingAction[F, G, Player, Inventory](
                breakCountReadAPI.seichiLevelUpdates
              ),
              PocketInventoryRepositoryDefinitions.finalization(persistence)
            )
        }
    } yield {
      new System[F, Player] {
        override val api: FourDimensionalPocketApi[F, Player] = new FourDimensionalPocketApi[F, Player] {
          override val openPocketInventory: Kleisli[F, Player, Unit] = Kleisli { player =>
            ContextCoercion {
              pocketInventoryRepositoryHandles
                .repository(player)._1
                .readLatest
            }.flatMap(inventory => interactInventory.open(inventory)(player))
          }
          override val currentPocketSize: KeyedDataRepository[Player, ReadOnlyRef[F, PocketSize]] =
            player => ReadOnlyRef.fromAnySource {
              pocketInventoryRepositoryHandles
                .repository(player)._1
                .readLatest
                .map(inventory => PocketSize.fromTotalStackCount(inventory.getSize))
            }
        }
        override val listeners: Seq[Listener] = Vector(
          pocketInventoryRepositoryHandles.initializer
        )
        override val managedFinalizers: Seq[PlayerDataFinalizer[F, Player]] = Vector(
          pocketInventoryRepositoryHandles.finalizer.coerceContextTo[F]
        )
        override val commands: Map[String, TabExecutor] = Map()
      }
    }
  }

}
