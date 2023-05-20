package com.github.unchama.seichiassist.subsystems.minestack

import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.minecraft.bukkit.algebra.BukkitPlayerHasUuid.instance
import com.github.unchama.minecraft.bukkit.objects.BukkitMaterial
import com.github.unchama.minecraft.objects.MinecraftMaterial
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gachaprize.GachaPrizeAPI
import com.github.unchama.seichiassist.subsystems.minestack.application.repository.{
  MineStackObjectRepositoryDefinition,
  MineStackSettingsRepositoryDefinition,
  MineStackUsageHistoryRepositoryDefinitions
}
import com.github.unchama.seichiassist.subsystems.minestack.bukkit.{
  BukkitMineStackObjectList,
  BukkitMineStackRepository,
  PlayerPickupItemListener
}
import com.github.unchama.seichiassist.subsystems.minestack.domain._
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectList,
  MineStackObjectWithAmount
}
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.{
  MineStackGachaObjectPersistence,
  PlayerSettingPersistence
}
import com.github.unchama.seichiassist.subsystems.minestack.infrastructure.{
  JdbcMineStackGachaObjectPersistence,
  JdbcMineStackObjectPersistence,
  JdbcPlayerSettingPersistence
}
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_], Player, ItemStack] extends Subsystem[F] {

  val api: MineStackAPI[F, Player, ItemStack]

}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[*[_], F]](
    implicit gachaPrizeAPI: GachaPrizeAPI[F, ItemStack, Player]
  ): F[System[F, Player, ItemStack]] = {
    implicit val mineStackGachaObjectPersistence
      : MineStackGachaObjectPersistence[F, ItemStack] =
      new JdbcMineStackGachaObjectPersistence[F, ItemStack, Player]
    implicit val minecraftMaterial: MinecraftMaterial[Material, ItemStack] = new BukkitMaterial
    implicit val _mineStackObjectList: MineStackObjectList[F, ItemStack, Player] =
      new BukkitMineStackObjectList[F]
    implicit val playerSettingPersistence: PlayerSettingPersistence[G] =
      new JdbcPlayerSettingPersistence[G]

    for {
      allMineStackObjects <- _mineStackObjectList.allMineStackObjects
      mineStackObjectPersistence = new JdbcMineStackObjectPersistence[G, ItemStack, Player](
        allMineStackObjects
      )
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
      implicit val mineStackObjectRepository
        : PlayerDataRepository[Ref[F, List[MineStackObjectWithAmount[ItemStack]]]] =
        mineStackObjectRepositoryControls.repository.map(_.mapK(ContextCoercion.asFunctionK))
      val mineStackUsageHistoryRepository = mineStackUsageHistoryRepositoryControls.repository
      implicit val mineStackSettingRepository
        : PlayerDataRepository[MineStackSettings[G, Player]] =
        mineStackSettingsRepositoryControls.repository
      implicit val _mineStackRepository: MineStackRepository[F, Player, ItemStack] =
        new BukkitMineStackRepository[F]

      new System[F, Player, ItemStack] {
        override val api: MineStackAPI[F, Player, ItemStack] =
          new MineStackAPI[F, Player, ItemStack] {
            override def getUsageHistory(
              player: Player
            ): F[Vector[MineStackObject[ItemStack]]] = ContextCoercion {
              mineStackUsageHistoryRepository(player).usageHistory
            }

            override def addUsageHistory(
              mineStackObject: MineStackObject[ItemStack]
            ): Kleisli[F, Player, Unit] = Kleisli { player =>
              ContextCoercion(
                mineStackUsageHistoryRepository(player).addHistory(mineStackObject)
              )
            }

            override def setAutoMineStack(
              isItemCollectedAutomatically: Boolean
            ): Kleisli[F, Player, Unit] = Kleisli { player =>
              for {
                _ <- ContextCoercion {
                  if (isItemCollectedAutomatically)
                    mineStackSettingRepository(player).turnOnAutoCollect
                  else mineStackSettingRepository(player).turnOffAutoCollect
                }
              } yield ()
            }

            override def toggleAutoMineStack: Kleisli[F, Player, Unit] =
              Kleisli { player =>
                for {
                  currentState <- autoMineStack(player)
                  _ <- setAutoMineStack(!currentState).apply(player)
                } yield ()
              }

            override def autoMineStack(player: Player): F[Boolean] = for {
              currentState <- ContextCoercion(
                mineStackSettingRepository(player).isAutoCollectionTurnedOn
              )
            } yield currentState

            override def mineStackObjectList: MineStackObjectList[F, ItemStack, Player] =
              _mineStackObjectList

            override def mineStackRepository: MineStackRepository[F, Player, ItemStack] =
              _mineStackRepository
          }

        override val listeners: Seq[Listener] = Seq(new PlayerPickupItemListener[F, G])

        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] = Seq(
          mineStackObjectRepositoryControls,
          mineStackUsageHistoryRepositoryControls,
          mineStackSettingsRepositoryControls
        ).map(_.coerceFinalizationContextTo[F])
      }

    }
  }
}
