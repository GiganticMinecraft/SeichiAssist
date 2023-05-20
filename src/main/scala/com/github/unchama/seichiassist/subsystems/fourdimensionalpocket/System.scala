package com.github.unchama.seichiassist.subsystems.fourdimensionalpocket

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Sync, SyncEffect, SyncIO}
import com.github.unchama.datarepository.KeyedDataRepository
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.effect.concurrent.ReadOnlyRef
import com.github.unchama.generic.effect.unsafe.EffectEnvironment
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.breakcount.BreakCountReadAPI
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.application.PocketInventoryRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.commands.{
  FourDimensionalPocketCommand,
  OpenPocketCommand
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.listeners.OpenPocketInventoryOnPlacingEnderPortalFrame
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.bukkit.{
  CreateBukkitInventory,
  InteractBukkitInventory
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.actions.{
  CreateInventory,
  InteractInventory
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.domain.{
  PocketInventoryPersistence,
  PocketSize
}
import com.github.unchama.seichiassist.subsystems.fourdimensionalpocket.infrastructure.JdbcBukkitPocketInventoryPersistence
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import io.chrisdavenport.log4cats.ErrorLogger
import org.bukkit.Sound
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

  def wired[F[_]: ConcurrentEffect: OnMinecraftServerThread: ErrorLogger, G[
    _
  ]: SyncEffect: ContextCoercion[*[_], F]](breakCountReadAPI: BreakCountReadAPI[F, G, Player])(
    implicit effectEnvironment: EffectEnvironment,
    syncIOUuidRepository: UuidRepository[SyncIO]
  ): F[System[F, Player]] = {
    val persistence: PocketInventoryPersistence[G, Inventory] =
      new JdbcBukkitPocketInventoryPersistence[G]

    implicit val createInventory: CreateInventory[G, Inventory] =
      new CreateBukkitInventory[G]

    implicit val interactInventory: InteractInventory[F, Player, Inventory] =
      new InteractBukkitInventory[F]

    for {
      pocketInventoryRepositoryHandles <-
        ContextCoercion {
          BukkitRepositoryControls.createHandles(
            PocketInventoryRepositoryDefinition
              .withContext(persistence, breakCountReadAPI.seichiLevelUpdates)
          )
        }
    } yield {
      implicit val systemApi = new FourDimensionalPocketApi[F, Player] {
        override val openPocketInventory: Kleisli[F, Player, Unit] = Kleisli { player =>
          Sync[F].delay {
            // 開く音を再生
            player.playSound(player.getLocation, Sound.BLOCK_ENDERCHEST_OPEN, 1f, 0.1f)
          } >> ContextCoercion {
            pocketInventoryRepositoryHandles.repository(player)._1.readLatest
          }.flatMap(inventory => interactInventory.open(inventory)(player))
        }
        override val currentPocketSize
          : KeyedDataRepository[Player, ReadOnlyRef[F, PocketSize]] = {
          KeyedDataRepository.unlift { player =>
            pocketInventoryRepositoryHandles.repository.lift(player).map {
              case (mutex, _) =>
                ReadOnlyRef.fromAnySource {
                  ContextCoercion {
                    mutex
                      .readLatest
                      .map(inventory => PocketSize.fromTotalStackCount(inventory.getSize))
                  }
                }
            }
          }
        }
      }

      val openPocketListener =
        new OpenPocketInventoryOnPlacingEnderPortalFrame[F](systemApi, effectEnvironment)

      val openPocketCommand =
        new OpenPocketCommand[F](
          pocketInventoryRepositoryHandles.repository.map {
            case (mutex, _) => ReadOnlyRef.fromAnySource(ContextCoercion(mutex.readLatest))
          },
          persistence.coerceContextTo[F]
        )
      new System[F, Player] {
        override val api: FourDimensionalPocketApi[F, Player] = systemApi
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          pocketInventoryRepositoryHandles.coerceFinalizationContextTo[F]
        )
        override val listeners: Seq[Listener] = Vector(openPocketListener)
        override val commands: Map[String, TabExecutor] = Map(
          "openpocket" -> openPocketCommand.executor,
          "fd" -> FourDimensionalPocketCommand.executor
        )
      }
    }
  }

}
