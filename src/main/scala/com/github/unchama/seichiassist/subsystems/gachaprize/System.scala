package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import cats.effect.ConcurrentEffect
import com.github.unchama.generic.serialization.SerializeAndDeserialize
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
  StaticGachaPrizeFactory
}
import com.github.unchama.seichiassist.subsystems.gachaprize.infrastructure.{
  JdbcGachaEventPersistence,
  JdbcGachaPrizeListPersistence
}
import com.github.unchama.seichiassist.subsystems.gachaprize.usecase.GachaEventUseCase
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait System[F[_]] extends Subsystem[F] {
  val api: GachaPrizeAPI[F, ItemStack, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: ConcurrentEffect]: System[F] = {
    implicit val _serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack] =
      BukkitItemStackSerializeAndDeserialize
    implicit val _gachaPersistence: GachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]
    implicit val _canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitItemStackCanBeSignedAsGachaPrize
    implicit val _staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      BukkitStaticGachaPrizeFactory
    implicit val _gachaEventPersistence: GachaEventPersistence[F] =
      new JdbcGachaEventPersistence[F]
    val gachaEventUseCase: GachaEventUseCase[F, ItemStack] = new GachaEventUseCase[F, ItemStack]

    new System[F] {
      override implicit val api: GachaPrizeAPI[F, ItemStack, Player] =
        new GachaPrizeAPI[F, ItemStack, Player] {
          override protected implicit val F: Monad[F] = implicitly

          override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean] = for {
            originalGachaPrizes <- _gachaPersistence.list
            _ <- _gachaPersistence.removeGachaPrize(gachaPrizeId)
          } yield originalGachaPrizes.exists(_.id == gachaPrizeId)

          override def addGachaPrize(gachaPrize: GachaPrizeByGachaPrizeId): F[Unit] = for {
            gachaPrizes <- _gachaPersistence.list
            gachaPrizeId = GachaPrizeId(
              if (gachaPrizes.nonEmpty) gachaPrizes.map(_.id.id).max + 1 else 1
            )
            newGachaPrize = gachaPrize(gachaPrizeId)
            _ = println(newGachaPrize.nonGachaEventItem)
            _ <- _gachaPersistence.addGachaPrize(newGachaPrize)
          } yield ()

          override def listOfNow: F[Vector[GachaPrize[ItemStack]]] = for {
            prizes <- _gachaPersistence.list
            createdEvents <- _gachaEventPersistence.gachaEvents
          } yield {
            createdEvents.find(_.isHolding) match {
              case Some(value) =>
                prizes.filter(_.gachaEvent.contains(value.eventName)) // :+ expBottle
              case None =>
                prizes.filter(_.nonGachaEventItem)
            }
          }

          override def allGachaPrizeList: F[Vector[GachaPrize[ItemStack]]] =
            _gachaPersistence.list

          override def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
            _staticGachaPrizeFactory

          override def createdGachaEvents: F[Vector[GachaEvent]] =
            _gachaEventPersistence.gachaEvents

          override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] =
            gachaEventUseCase.createGachaEvent(gachaEvent)

          override def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit] =
            _gachaEventPersistence.deleteGachaEvent(gachaEventName)

          override def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
            _canBeSignedAsGachaPrize
        }
    }
  }

}
