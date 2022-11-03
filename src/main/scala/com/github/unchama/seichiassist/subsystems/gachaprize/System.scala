package com.github.unchama.seichiassist.subsystems.gachaprize

import cats.Monad
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{BukkitDrawGacha, BukkitGrantGachaPrize}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.{GachaCommand, PlayerPullGachaListener}
import com.github.unchama.seichiassist.subsystems.gacha.domain.LotteryOfGachaItems
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketFromAdminTeamRepository
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamRepository
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
import com.github.unchama.seichiassist.subsystems.minestack.MineStackAPI
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_]] extends Subsystem[F] {
  val api: GachaAPI[F, ItemStack, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect](
    implicit gachaTicketAPI: GachaTicketAPI[F],
    mineStackAPI: MineStackAPI[F, Player, ItemStack]
  ): F[System[F]] = {
    implicit val _serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack] =
      BukkitItemStackSerializeAndDeserialize
    implicit val _gachaPersistence: GachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]()
    implicit val _gachaTicketPersistence: GachaTicketFromAdminTeamRepository[F] =
      new JdbcGachaTicketFromAdminTeamRepository[F]
    implicit val _canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitItemStackCanBeSignedAsGachaPrize
    implicit val _staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      BukkitStaticGachaPrizeFactory
    implicit val _lotteryOfGachaItems: LotteryOfGachaItems[F, ItemStack] =
      new LotteryOfGachaItems[F, ItemStack]
    implicit val _grantGachaPrize: GrantGachaPrize[F, ItemStack] =
      new BukkitGrantGachaPrize[F]
    val _gachaEventPersistence: GachaEventPersistence[F] = new JdbcGachaEventPersistence[F]

    val system: F[System[F]] = {
      for {
        gachaPrizesListReference <- Ref.of[F, Vector[GachaPrize[ItemStack]]](Vector.empty)
      } yield {
        new System[F] {
          override implicit val api: GachaAPI[F, ItemStack, Player] =
            new GachaAPI[F, ItemStack, Player] {
              override protected implicit val F: Monad[F] = implicitly

              override def load: F[Unit] = for {
                gachaPrizes <- _gachaPersistence.list
                createdEvents <- _gachaEventPersistence.gachaEvents
                targetGachaPrizes <- Sync[F].delay {
                  createdEvents.find(_.isHolding) match {
                    case Some(value) =>
                      gachaPrizes.filter(_.gachaEventName.contains(value.eventName))
                    case None =>
                      gachaPrizes.filter(_.gachaEventName.isEmpty)
                  }
                }
                _ <- gachaPrizesListReference.set(targetGachaPrizes)
              } yield ()

              override def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] =
                gachaPrizesListReference.set(gachaPrizesList)

              override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] =
                gachaPrizesListReference.update { prizes =>
                  prizes.filter(_.id == gachaPrizeId)
                }

              override def addGachaPrize(gachaPrize: GachaPrizeByGachaPrizeId): F[Unit] =
                gachaPrizesListReference.update { prizes =>
                  gachaPrize(
                    GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1)
                  ) +: prizes
                }

              override val grantGachaPrize: GrantGachaPrize[F, ItemStack] =
                new BukkitGrantGachaPrize[F]

              override def list: F[Vector[GachaPrize[ItemStack]]] = gachaPrizesListReference.get

              override def drawGacha(player: Player, draws: Int): F[Unit] =
                new BukkitDrawGacha[F](gachaPrizesListReference).draw(player, draws)

              override def staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
                _staticGachaPrizeFactory

              override def createdGachaEvents: F[Vector[GachaEvent]] =
                _gachaEventPersistence.gachaEvents

              override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] = {
                _gachaEventPersistence.createGachaEvent(gachaEvent) >> (for {
                  prizes <- list
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

              override def findByItemStack(
                itemStack: ItemStack
              ): F[Option[GachaPrize[ItemStack]]] = for {
                prizes <- gachaPrizesListReference.get
              } yield prizes.filter(_.gachaEventName.isEmpty).find(_.itemStack == itemStack)

            }
          override val commands: Map[String, TabExecutor] = Map(
            "gacha" -> new GachaCommand[F]().executor
          )
          override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F]())

        }
      }
    }

    for {
      system <- system
      _ <- system.api.load
    } yield system
  }

}
