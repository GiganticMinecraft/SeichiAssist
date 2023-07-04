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
import com.github.unchama.seichiassist.subsystems.gachaprize.usecase.GachaPrizeUseCase
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

trait System[F[_]] extends Subsystem[F] {
  val api: GachaPrizeAPI[F, ItemStack, Player]
}

object System {

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
    val gachaPrizeUseCase: GachaPrizeUseCase[F, ItemStack] = new GachaPrizeUseCase[F, ItemStack]

    new System[F] {
      override implicit val api: GachaPrizeAPI[F, ItemStack, Player] =
        new GachaPrizeAPI[F, ItemStack, Player] {
          override protected implicit val F: Monad[F] = implicitly

          override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Boolean] =
            gachaPrizeUseCase.removeByGachaPrizeId(gachaPrizeId)

          override def addGachaPrize(
            gachaPrizeByGachaPrizeId: GachaPrizeByGachaPrizeId
          ): F[Unit] =
            gachaPrizeUseCase.addGachaPrize(gachaPrizeByGachaPrizeId)

          override def listOfNow: F[Vector[GachaPrize[ItemStack]]] =
            gachaPrizeUseCase.listOfNow

          override def allGachaPrizeList: F[Vector[GachaPrize[ItemStack]]] =
            _gachaPersistence.list

          override def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
            _staticGachaPrizeFactory

          override def createdGachaEvents: F[Vector[GachaEvent]] =
            _gachaEventPersistence.gachaEvents

          override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] =
            gachaPrizeUseCase.createGachaEvent(gachaEvent)

          override def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit] =
            _gachaEventPersistence.deleteGachaEvent(gachaEventName)

          override def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
            _canBeSignedAsGachaPrize
        }
    }
  }

}
