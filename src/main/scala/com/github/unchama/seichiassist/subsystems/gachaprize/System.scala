package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
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
  StaticGachaPrizeFactory
}
import com.github.unchama.seichiassist.subsystems.gachaprize.infrastructure.{
  JdbcGachaEventPersistence,
  JdbcGachaPrizeListPersistence
}
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

    val system: F[System[F]] = {
      for {
        gachaPrizes <- _gachaPersistence.list
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
                  createdEvents.find(_.isHolding) match {
                    case Some(value) =>
                      prizes.filter(_.gachaEventName.contains(value.eventName))
                    case None =>
                      prizes.filter(_.gachaEventName.isEmpty)
                  }
                }
              } yield ()

              override def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] =
                allGachaPrizesListReference.set(gachaPrizesList)

              override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] = for {
                _ <- allGachaPrizesListReference.update { prizes =>
                  prizes.filter(_.id == gachaPrizeId)
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
                _ <- _gachaPersistence
                  .addMineStackGachaObject(
                    newGachaPrizes.head.id,
                    s"gachadata0_${newGachaPrizes.head.id.id - 1}"
                  )
                  .whenA(newGachaPrizes.head.gachaEventName.isEmpty)
              } yield ()

              override def listOfNow: F[Vector[GachaPrize[ItemStack]]] =
                allGachaPrizesListReference.get

              override def allGachaPrizeList: F[Vector[GachaPrize[ItemStack]]] =
                allGachaPrizesListReference.get

              override def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
                _staticGachaPrizeFactory

              override def createdGachaEvents: F[Vector[GachaEvent]] =
                _gachaEventPersistence.gachaEvents

              override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] = {
                _gachaEventPersistence.createGachaEvent(gachaEvent) >> (for {
                  prizes <- allGachaPrizesListReference.get
                  defaultGachaPrizes = prizes
                    .filter(_.gachaEventName.isEmpty)
                    .map(_.copy(gachaEventName = Some(gachaEvent.eventName)))
                  _ <- replace(defaultGachaPrizes ++ prizes)
                } yield ())
              }

              override def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit] =
                _gachaEventPersistence.deleteGachaEvent(gachaEventName)

              override def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
                _canBeSignedAsGachaPrize

              override def findOfRegularPrizesByItemStack(
                itemStack: ItemStack,
                name: String
              ): F[Option[GachaPrize[ItemStack]]] = for {
                prizes <- allGachaPrizesListReference.get
                defaultGachaPrizes = prizes.filter(_.gachaEventName.isEmpty)
              } yield defaultGachaPrizes.find(_.itemStack.isSimilar(itemStack))
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
