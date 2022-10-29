package com.github.unchama.seichiassist.subsystems.gacha

import cats.Monad
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{
  BukkitDrawGacha,
  BukkitGrantGachaPrize,
  BukkitLotteryOfGachaItems
}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaevent.{
  GachaEvent,
  GachaEventName,
  GachaEventPersistence
}
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrize,
  GachaPrizeId,
  GachaPrizeListPersistence
}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.{
  JdbcGachaEventPersistence,
  JdbcGachaPrizeListPersistence
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketFromAdminTeamRepository
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamRepository
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_]] extends Subsystem[F] {
  val api: GachaAPI[F, ItemStack, Player]
}

object System {

  import cats.implicits._

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect]
    : F[System[F]] = {
    implicit val serializeAndDeserialize: SerializeAndDeserialize[Nothing, ItemStack] =
      BukkitItemStackSerializeAndDeserialize
    implicit val gachaPersistence: GachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]()
    implicit val gachaTicketPersistence: GachaTicketFromAdminTeamRepository[F] =
      new JdbcGachaTicketFromAdminTeamRepository[F]
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitCanBeSignedAsGachaPrize
    implicit val lotteryOfGachaItems: BukkitLotteryOfGachaItems[F] =
      new BukkitLotteryOfGachaItems[F]

    val gachaEventPersistence: GachaEventPersistence[F] = new JdbcGachaEventPersistence[F]

    val system: System[F] = new System[F] {
      override implicit val api: GachaAPI[F, ItemStack, Player] =
        new GachaAPI[F, ItemStack, Player] {

          override protected implicit val F: Monad[F] = implicitly

          override def load: F[Unit] = gachaPersistence.list.flatMap { gachaPrizes =>
            gachaPrizesListRepository.set(gachaPrizes)
          }

          override def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] =
            gachaPrizesListRepository.set(gachaPrizesList)

          override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] = for {
            prizes <- list
            targetPrize = prizes.filter(_.id == gachaPrizeId)
            _ <- replace(prizes.diff(targetPrize))
          } yield ()

          override def addGachaPrize(
            gachaPrize: GachaPrizeByGachaPrizeId
          ): F[Unit] =
            for {
              prizes <- list
              newList = gachaPrize(
                GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1)
              ) +: prizes

              _ <- replace(newList)
            } yield ()

          protected implicit val gachaPrizesListRepository
            : Ref[F, Vector[GachaPrize[ItemStack]]] =
            Ref.unsafe[F, Vector[GachaPrize[ItemStack]]](Vector.empty)

          override val grantGachaPrize: GrantGachaPrize[F, ItemStack] =
            new BukkitGrantGachaPrize[F]

          override def list: F[Vector[GachaPrize[ItemStack]]] = gachaPrizesListRepository.get

          override def drawGacha(player: Player, draws: Int): F[Unit] =
            new BukkitDrawGacha[F].draw(player, draws)

          override def alwaysDischargeGachaPrizes: F[Vector[GachaPrize[ItemStack]]] =
            gachaPersistence.alwaysDischargeGachaPrizes

          override def getOnlyGachaEventDischargeGachaPrizes(
            gachaEventName: GachaEventName
          ): F[Vector[GachaPrize[ItemStack]]] =
            gachaPersistence.getOnlyGachaEventDischargeGachaPrizes(gachaEventName)

          override def createdGachaEvents: F[Vector[GachaEvent]] =
            gachaEventPersistence.gachaEvents

          override def createGachaEvent(gachaEvent: GachaEvent): F[Unit] =
            gachaEventPersistence.createGachaEvent(gachaEvent)

          override def deleteGachaEvent(gachaEventName: GachaEventName): F[Unit] =
            gachaEventPersistence.deleteGachaEvent(gachaEventName)
        }
      override val commands: Map[String, TabExecutor] = Map(
        "gacha" -> new GachaCommand[F]().executor
      )
      override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F]())
    }

    for {
      _ <- system.api.load
    } yield system
  }

}
