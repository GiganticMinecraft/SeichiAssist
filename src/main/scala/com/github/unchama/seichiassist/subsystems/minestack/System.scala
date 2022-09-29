package com.github.unchama.seichiassist.subsystems.minestack

import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.generic.{ContextCoercion, ListExtra}
import com.github.unchama.minecraft.bukkit.objects.{BukkitItemStack, BukkitMaterial}
import com.github.unchama.minecraft.objects.{MinecraftItemStack, MinecraftMaterial}
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.minestack.application.repository.MineStackObjectRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.minestack.bukkit.MineStackObjectList
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectWithAmount
}
import com.github.unchama.seichiassist.subsystems.minestack.infrastructure.JdbcMineStackObjectPersistence
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait System[F[_], Player, ItemStack] extends Subsystem[F] {

  val api: MineStackAPI[F, Player, ItemStack]

}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect, G[_]: SyncEffect: ContextCoercion[*[_], F]]
    : F[System[F, Player, ItemStack]] = {
    implicit val minecraftItemStack: MinecraftItemStack[ItemStack] = new BukkitItemStack
    implicit val minecraftMaterial: MinecraftMaterial[Material, ItemStack] = new BukkitMaterial
    for {
      allMineStackObjects <- new MineStackObjectList[F]().getAllMineStackObjects
      mineStackObjectPersistence =
        new JdbcMineStackObjectPersistence[G, ItemStack](allMineStackObjects)

      mineStackObjectRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          MineStackObjectRepositoryDefinition
            .withContext[G, Player, ItemStack](mineStackObjectPersistence)
        )
      )
    } yield {
      val mineStackObjectRepository =
        mineStackObjectRepositoryControls.repository.map(_.mapK(ContextCoercion.asFunctionK))
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
          }
      }

    }
  }
}
