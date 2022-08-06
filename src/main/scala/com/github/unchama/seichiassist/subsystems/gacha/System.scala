package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, Sync}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{BukkitGrantGachaPrize, BukkitLotteryOfGachaItems}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain.{GachaPrize, GachaPrizeId}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.JdbcGachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamGateway
import org.bukkit.command.TabExecutor
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

trait System[F[_]] extends Subsystem[F] {
  val api: GachaAPI[F, ItemStack]
}

object System {

  import cats.implicits._

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect]
    : F[System[F]] = {
    implicit val serializeAndDeserialize: SerializeAndDeserialize[Unit, ItemStack] =
      BukkitItemStackSerializeAndDeserialize.instance
    implicit val gachaPersistence: JdbcGachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]()
    implicit val gachaTicketPersistence: JdbcGachaTicketFromAdminTeamGateway[F] =
      new JdbcGachaTicketFromAdminTeamGateway[F]

    val system = new System[F] {
      override implicit val api: GachaAPI[F, ItemStack] = new GachaAPI[F, ItemStack] {

        override protected implicit val _FSync: Sync[F] = implicitly[ConcurrentEffect[F]]

        override def load: F[Unit] = gachaPersistence.list.flatMap { gachaPrizes =>
          gachaPrizesListRepository.set(gachaPrizes)
        }

        override def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] =
          gachaPrizesListRepository.set(gachaPrizesList)

        override def runLottery(amount: Int): F[Vector[GachaPrize[ItemStack]]] =
          new BukkitLotteryOfGachaItems[F].runLottery(amount, gachaPrizesListRepository)

        override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] = for {
          prizes <- list
          targetPrize = prizes.filter(_.id == gachaPrizeId)
          _ <- replace(prizes.diff(targetPrize))
        } yield ()

        /**
         * `GachaPrize`を追加する。
         * `GachaPrizeId`を与えなかった場合は最大`GachaPrizeId`の次の値が指定されます
         */
        override def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize[ItemStack]): F[Unit] =
          for {
            prizes <- list
            newList = prizes ++ Vector(
              gachaPrize(GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1))
            )
            _ <- replace(newList)
          } yield ()

        override protected val gachaPrizesListRepository
          : Ref[F, Vector[GachaPrize[ItemStack]]] =
          Ref.unsafe[F, Vector[GachaPrize[ItemStack]]](Vector.empty)

        override val grantGachaPrize: GachaPrize[ItemStack] => GrantGachaPrize[F, ItemStack] =
          new BukkitGrantGachaPrize[F](_)

      }
      override val commands: Map[String, TabExecutor] = Map(
        "gacha" -> new GachaCommand[F]().executor
      )
      override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F]())
    }

    system.api.load.map { _ => system }
  }

}
