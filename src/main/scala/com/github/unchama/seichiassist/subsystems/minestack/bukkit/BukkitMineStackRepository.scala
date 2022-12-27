package com.github.unchama.seichiassist.subsystems.minestack.bukkit

import cats.effect.Sync
import cats.effect.concurrent.Ref
import com.github.unchama.datarepository.bukkit.player.PlayerDataRepository
import com.github.unchama.generic.ListExtra
import com.github.unchama.seichiassist.subsystems.minestack.domain.MineStackRepository
import com.github.unchama.seichiassist.subsystems.minestack.domain.minestackobject.{
  MineStackObject,
  MineStackObjectList,
  MineStackObjectWithAmount
}
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BukkitMineStackRepository[F[_]: Sync](
  implicit mineStackObjectList: MineStackObjectList[F, ItemStack, Player],
  mineStackObjectRepository: PlayerDataRepository[
    Ref[F, List[MineStackObjectWithAmount[ItemStack]]]
  ]
) extends MineStackRepository[F, Player, ItemStack] {

  import cats.implicits._

  override def addStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Unit] = mineStackObjectRepository(player).update { mineStackObjects =>
    ListExtra
      .rePrepend(mineStackObjects)(_.mineStackObject == mineStackObject, _.increase(amount))
  }

  override def subtractStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Long
  ): F[Long] = for {
    oldMineStackObjects <- mineStackObjectRepository(player).get
    updatedMineStackObjects <- mineStackObjectRepository(player).updateAndGet {
      mineStackObjects =>
        ListExtra
          .rePrepend(mineStackObjects)(_.mineStackObject == mineStackObject, _.decrease(amount))
    }
  } yield {
    ListExtra.findBothThenMap(oldMineStackObjects, updatedMineStackObjects)(
      _.mineStackObject == mineStackObject,
      _.fold(0L) {
        case (oldMineStackObject, updatedMineStackObject) =>
          Math.abs(oldMineStackObject.amount - updatedMineStackObject.amount)
      }
    )
  }

  override def tryIntoMineStack(player: Player, itemStack: ItemStack, amount: Int): F[Boolean] =
    for {
      foundMineStackObject <- mineStackObjectList.findByItemStack(itemStack, player)
      _ <- foundMineStackObject.traverse(addStackedAmountOf(player, _, amount))
    } yield foundMineStackObject.nonEmpty
}
