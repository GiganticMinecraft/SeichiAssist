package com.github.unchama.seichiassist.subsystems.minestack

import cats.effect.{ConcurrentEffect, Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.{ContextCoercion, ListExtra}
import com.github.unchama.minecraft.bukkit.objects.BukkitMaterial
import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.minestack.application.repository.{MineStackObjectRepositoryDefinition, MineStackSettingsRepositoryDefinition, MineStackUsageHistoryRepositoryDefinitions}
import com.github.unchama.seichiassist.subsystems.minestack.bukkit.BukkitMineStackObjectList
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{MineStackObject, MineStackObjectWithAmount}
import com.github.unchama.seichiassist.subsystems.minestack.infrastructure.JdbcMineStackObjectPersistence
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait System[F[_], Player, ItemStack <: Cloneable] extends Subsystem[F] {

  val api: MineStackAPI[F, Player, ItemStack]

}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[*[_], F]]
    : F[System[F, Player, ItemStack]] = {
    implicit val minecraftMaterial: MinecraftMaterial[Material, ItemStack] = new BukkitMaterial
    for {
      allMineStackObjects <- new BukkitMineStackObjectList[F]().getAllMineStackObjects
      mineStackObjectPersistence =
        new JdbcMineStackObjectPersistence[G, ItemStack](allMineStackObjects)

      mineStackObjectRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          MineStackObjectRepositoryDefinition
            .withContext[G, Player, ItemStack](mineStackObjectPersistence)
        )
      )

      mineStackUsageHistoryRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              MineStackUsageHistoryRepositoryDefinitions.initialization[G, Player, ItemStack],
              MineStackUsageHistoryRepositoryDefinitions.finalization[G, Player, ItemStack]
            )
        )
      )

      mineStackSettingsRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              MineStackSettingsRepositoryDefinition.initialization[G, Player],
              MineStackSettingsRepositoryDefinition.finalization[G, Player]
            )
        )
      )
    } yield {
      val mineStackObjectRepository =
        mineStackObjectRepositoryControls.repository.map(_.mapK(ContextCoercion.asFunctionK))
      val mineStackUsageHistoryRepository = mineStackUsageHistoryRepositoryControls.repository
      val mineStackSettingRepository = mineStackSettingsRepositoryControls.repository

      new System[F, Player, ItemStack] {
        override val api: MineStackAPI[F, Player, ItemStack] =
          new MineStackAPI[F, Player, ItemStack] {
            override def addStackedAmountOf(
              player: Player,
              mineStackObject: MineStackObject[ItemStack],
              amount: Int
            ): F[Unit] =
              mineStackObjectRepository(player).update { mineStackObjects =>
                ListExtra.rePrependOrAdd(mineStackObjects)(
                  _.mineStackObject == mineStackObject,
                  {
                    case Some(value) => value.increase(amount)
                    case None        => MineStackObjectWithAmount(mineStackObject, amount)
                  }
                )
              }

            override def trySubtractStackedAmountOf(
              player: Player,
              mineStackObject: MineStackObject[ItemStack],
              amount: Int
            ): F[Int] = {
              for {
                oldMineStackObjects <- mineStackObjectRepository(player).get
                updatedMineStackObjects <- mineStackObjectRepository(player).updateAndGet {
                  mineStackObjects =>
                    ListExtra.rePrepend(mineStackObjects)(
                      _.mineStackObject == mineStackObject,
                      _.decrease(amount)
                    )
                }
              } yield {
                ListExtra.computeDoubleList(oldMineStackObjects, updatedMineStackObjects)(
                  _.mineStackObject == mineStackObject,
                  {
                    case Some((oldMineStackObject, updatedMineStackObject)) =>
                      Math.abs(oldMineStackObject.amount - updatedMineStackObject.amount)
                    case None => 0
                  }
                )
              }
            }

            override def getStackedAmountOf(
              player: Player,
              mineStackObject: MineStackObject[ItemStack]
            ): F[Long] = for {
              mineStackObjects <- mineStackObjectRepository(player).get
            } yield {
              mineStackObjects.find(_.mineStackObject == mineStackObject).getOrElse(0)
            }

            override def getUsageHistory(player: Player): Vector[MineStackObject[ItemStack]] =
              mineStackUsageHistoryRepository(player).usageHistory

            override def addUsageHistory(
              player: Player,
              mineStackObject: MineStackObject[ItemStack]
            ): F[Unit] = Sync[F].delay {
              mineStackUsageHistoryRepository(player).addHistory(mineStackObject)
            }

            override def toggleAutoMineStack(player: Player): F[Unit] = for {
              currentState <- autoMineStack(player)
              _ <- ContextCoercion {
                if (currentState)
                  mineStackSettingRepository(player).toggleAutoMineStackTurnOff
                else mineStackSettingRepository(player).toggleAutoMineStackTurnOn
              }
            } yield ()

            override def autoMineStack(player: Player): F[Boolean] = for {
              currentState <- ContextCoercion(mineStackSettingRepository(player).currentState)
            } yield currentState
          }
      }

    }
  }
}
