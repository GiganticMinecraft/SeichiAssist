package com.github.unchama.seichiassist.subsystems.gacha

import cats.Functor
import cats.effect.concurrent.Ref
import cats.effect.ConcurrentEffect
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{
  BukkitGrantGachaPrize,
  BukkitLotteryOfGachaItems
}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  CanBeSignedAsGachaPrize,
  GachaPrize,
  GachaPrizeId,
  GachaPrizeListPersistence
}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.JdbcGachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketFromAdminTeamRepository
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamRepository
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
      BukkitItemStackSerializeAndDeserialize
    implicit val gachaPersistence: GachaPrizeListPersistence[F, ItemStack] =
      new JdbcGachaPrizeListPersistence[F, ItemStack]()
    implicit val gachaTicketPersistence: GachaTicketFromAdminTeamRepository[F] =
      new JdbcGachaTicketFromAdminTeamRepository[F]
    implicit val canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
      BukkitCanBeSignedAsGachaPrize

    val system = new System[F] {
      override implicit val api: GachaAPI[F, ItemStack] = new GachaAPI[F, ItemStack] {

        override protected implicit val _FFunctor: Functor[F] = implicitly[ConcurrentEffect[F]]

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

        override def addGachaPrize(gachaPrize: GachaPrizeId => GachaPrize[ItemStack]): F[Unit] =
          for {
            prizes <- list
            newList = prizes ++ Vector(
              gachaPrize(GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1))
            )
            _ <- replace(newList)
          } yield ()

        protected val gachaPrizesListRepository: Ref[F, Vector[GachaPrize[ItemStack]]] =
          Ref.unsafe[F, Vector[GachaPrize[ItemStack]]](Vector.empty)

        override val grantGachaPrize: GrantGachaPrize[F, ItemStack] =
          new BukkitGrantGachaPrize[F]

        override def list: F[Vector[GachaPrize[ItemStack]]] = gachaPrizesListRepository.get
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
