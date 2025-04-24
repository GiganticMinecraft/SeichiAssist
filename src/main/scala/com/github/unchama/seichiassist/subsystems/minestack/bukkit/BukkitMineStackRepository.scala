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

  override def getStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack]
  ): F[Long] = for {
    mineStackObjects <- mineStackObjectRepository(player).get
  } yield {
    mineStackObjects.find(_.mineStackObject == mineStackObject).map(_.amount).getOrElse(0L)
  }

  override def addStackedAmountOf(
    player: Player,
    mineStackObject: MineStackObject[ItemStack],
    amount: Int
  ): F[Unit] = mineStackObjectRepository(player).update { mineStackObjects =>
    ListExtra.rePrependOrAdd(mineStackObjects)(
      _.mineStackObject == mineStackObject,
      {
        case Some(value) => value.increase(amount)
        case None        => MineStackObjectWithAmount(mineStackObject, amount)
      }
    )
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

  override def tryIntoMineStack(
    player: Player,
    itemStack: ItemStack,
    amount: Int
  ): F[Boolean] = {
    for {
      foundMineStackObject <- mineStackObjectList.findBySignedItemStacks(
        Vector(itemStack),
        player
      )
      _ <- foundMineStackObject.head.traverse {
        case Some(mineStackObject) =>
          addStackedAmountOf(player, mineStackObject, amount)
        case None => Sync[F].unit
      }
    } yield foundMineStackObject.head._2.isDefined
  }

  override def tryIntoMineStack(
    player: Player,
    itemStacks: Vector[ItemStack]
  ): F[(Vector[ItemStack], Vector[ItemStack])] = {
    for {
      mineStackObjects <- mineStackObjectList.findBySignedItemStacks(itemStacks, player)
      _ <- mineStackObjects.traverse {
        case (itemStack, Some(mineStackObject)) =>
          addStackedAmountOf(player, mineStackObject, itemStack.getAmount)
        case _ => Sync[F].unit
      }
    } yield mineStackObjects.partitionMap {
      case (itemStack, Some(_)) => Right(itemStack)
      case (itemStack, None)    => Left(itemStack)
    }
  }
}
