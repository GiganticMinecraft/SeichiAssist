package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.minecraft.bukkit.algebra.CloneableBukkitItemStack.instance
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.BukkitItemStackCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gachaprize.bukkit.factories.BukkitStaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaevent.{
  GachaEvent,
  GachaEventName,
  GachaEventPersistence
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}
import com.github.unchama.seichiassist.subsystems.gachaprize.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrizeListPersistence,
  GachaProbability,
  StaticGachaPrizeFactory
}
import com.github.unchama.seichiassist.subsystems.gachaprize.infrastructure.{
  JdbcGachaEventPersistence,
  JdbcGachaPrizeListPersistence
}
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait System[F[_]] extends Subsystem[F] {
  val api: GachaPrizeAPI[F, ItemStack, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect]
    : F[System[F]] = {
    implicit val _serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack] =
      BukkitItemStackSerializeAndDeserialize
    implicit val _gachaPersistence: GachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]()
    implicit val _canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitItemStackCanBeSignedAsGachaPrize
    implicit val _staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      BukkitStaticGachaPrizeFactory
    val _gachaEventPersistence: GachaEventPersistence[F] = new JdbcGachaEventPersistence[F]

    val system: F[System[F]] = for {
      persistedGachaPrizes <- _gachaPersistence.list
      expBottle = GachaPrize(
        new ItemStack(Material.EXP_BOTTLE, 20),
        GachaProbability(0.1),
        signOwner = false,
        GachaPrizeId(2),
        None
      )
      gachaPrizes = expBottle +: persistedGachaPrizes
      gachaPrizesListReference <- Ref.of[F, Vector[GachaPrize[ItemStack]]](gachaPrizes)
      allGachaPrizesListReference <- Ref.of[F, Vector[GachaPrize[ItemStack]]](gachaPrizes)
    } yield {
      new System[F] {
        override implicit val api: GachaPrizeAPI[F, ItemStack, Player] =
          new GachaPrizeAPI[F, ItemStack, Player] {
            override protected implicit val F: Monad[F] = implicitly

            override def load: F[Unit] = for {
              createdEvents <- _gachaEventPersistence.gachaEvents
              _ <- gachaPrizesListReference.update { prizes =>
                createdEvents
                  .find(_.isHolding)
                  .fold(prizes.filter(_.gachaEventName.isEmpty))(value =>
                    prizes.filter(_.gachaEventName.contains(value.eventName))
                  )
              }
            } yield ()

            override def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] =
              allGachaPrizesListReference.set(gachaPrizesList)

            override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] = for {
              _ <- allGachaPrizesListReference.update { prizes =>
                prizes.filterNot(_.id == gachaPrizeId)
              }
              _ <- _gachaPersistence.deleteMineStackGachaObject(gachaPrizeId)
            } yield ()

            override def addGachaPrize(gachaPrize: GachaPrizeByGachaPrizeId): F[Unit] = for {
              _ <- allGachaPrizesListReference.update { prizes =>
                gachaPrize(
                  GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1)
                ) +: prizes
              }
              newGachaPrizes <- allGachaPrizesListReference.get
              _ <- _gachaPersistence.addGachaPrize(newGachaPrizes.head)
              _ <- _gachaPersistence
                .addMineStackGachaObject(
                  newGachaPrizes.head.id,
                  s"gachadata0_${newGachaPrizes.head.id.id - 1}"
                )
                .whenA(newGachaPrizes.head.gachaEventName.isEmpty)
            } yield ()

            override def listOfNow: F[Vector[GachaPrize[ItemStack]]] = for {
              prizes <- allGachaPrizesListReference.get
              createdEvents <- _gachaEventPersistence.gachaEvents
            } yield {
              createdEvents.find(_.isHolding) match {
                case Some(value) =>
                  prizes.filter(_.gachaEventName.contains(value.eventName)) :+ expBottle
                case None =>
                  prizes.filter(_.gachaEventName.isEmpty)
              }
            }

            override def allGachaPrizeList: F[Vector[GachaPrize[ItemStack]]] =
              allGachaPrizesListReference.get

            override def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
              _staticGachaPrizeFactory

            override def createdGachaEvents: F[Vector[GachaEvent]] =
              _gachaEventPersistence.gachaEvents

            override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] = {
              for {
                _ <- _gachaEventPersistence.createGachaEvent(gachaEvent)
                currentAllGachaPrizes <- allGachaPrizesListReference.get
                maxId = currentAllGachaPrizes.map(_.id.id).max
                eventGachaPrizes = currentAllGachaPrizes
                  .filter(_.gachaEventName.isEmpty)
                  .map(gachaPrize =>
                    gachaPrize.copy(
                      gachaEventName = Some(gachaEvent.eventName),
                      id = GachaPrizeId(maxId + gachaPrize.id.id)
                    )
                  )
                _ <- replace(eventGachaPrizes ++ currentAllGachaPrizes)
                _ <- _gachaPersistence.addGachaPrizes(eventGachaPrizes)
              } yield ()
            }

            override def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit] =
              _gachaEventPersistence.deleteGachaEvent(gachaEventName)

            override def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
              _canBeSignedAsGachaPrize

            override def findOfRegularPrizesBySignedItemStack(
              itemStack: ItemStack,
              name: String
            ): F[Option[GachaPrize[ItemStack]]] = for {
              prizes <- allGachaPrizesListReference.get
              defaultGachaPrizes = prizes.filter(_.gachaEventName.isEmpty)
            } yield defaultGachaPrizes.find { gachaPrize =>
              if (gachaPrize.signOwner) {
                gachaPrize.materializeWithOwnerSignature(name).isSimilar(itemStack)
              } else {
                gachaPrize.itemStack.isSimilar(itemStack)
              }
            }

            override def findOfRegularGachaPrizesByNotSignedItemStack(
              itemStack: ItemStack
            ): F[Option[GachaPrize[ItemStack]]] = for {
              prizes <- allGachaPrizesListReference.get
              defaultGachaPrizes = prizes.filter(_.gachaEventName.isEmpty)
            } yield defaultGachaPrizes.find { gachaPrize =>
              gachaPrize.itemStack.isSimilar(itemStack)
            }
          }

      }
    }

    for {
      system <- system
      _ <- system.api.load
    } yield system
  }

}
