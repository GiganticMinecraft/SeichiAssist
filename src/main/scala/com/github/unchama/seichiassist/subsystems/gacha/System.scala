package com.github.unchama.seichiassist.subsystems.gacha

import cats.Functor
import cats.effect.ConcurrentEffect
import cats.effect.concurrent.Ref
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitItemStackCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{
  BukkitDrawGacha,
  BukkitGrantGachaPrize
}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.factories.BukkitStaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.{GachaPrize, GachaPrizeId}
import com.github.unchama.seichiassist.subsystems.gacha.domain.{LotteryOfGachaItems, CanBeSignedAsGachaPrize, GachaPrizeListPersistence, StaticGachaPrizeFactory}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.JdbcGachaPrizeListPersistence
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
      BukkitItemStackCanBeSignedAsGachaPrize
    implicit val staticGachaPrizeFactory: StaticGachaPrizeFactory[ItemStack] =
      BukkitStaticGachaPrizeFactory
    implicit val lotteryOfGachaItems: LotteryOfGachaItems[F, ItemStack] =
      new LotteryOfGachaItems[F, ItemStack]

    val system: System[F] = new System[F] {
      override implicit val api: GachaAPI[F, ItemStack, Player] =
        new GachaAPI[F, ItemStack, Player] {

          override protected implicit val F: Functor[F] = implicitly

          override def load: F[Unit] = gachaPersistence.list.flatMap { gachaPrizes =>
            gachaPrizesListReference.set(gachaPrizes)
          }

          override def replace(gachaPrizesList: Vector[GachaPrize[ItemStack]]): F[Unit] =
            gachaPrizesListReference.set(gachaPrizesList)

          override def removeByGachaPrizeId(gachaPrizeId: GachaPrizeId): F[Unit] =
            gachaPrizesListReference.update { prizes => prizes.filter(_.id == gachaPrizeId) }

          override def addGachaPrize(gachaPrize: GachaPrizeByGachaPrizeId): F[Unit] =
            gachaPrizesListReference.update { prizes =>
              gachaPrize(
                GachaPrizeId(if (prizes.nonEmpty) prizes.map(_.id.id).max + 1 else 1)
              ) +: prizes
            }

          protected implicit val gachaPrizesListReference
            : Ref[F, Vector[GachaPrize[ItemStack]]] =
            Ref.unsafe[F, Vector[GachaPrize[ItemStack]]](Vector.empty)

          override val grantGachaPrize: GrantGachaPrize[F, ItemStack] =
            new BukkitGrantGachaPrize[F]

          override def list: F[Vector[GachaPrize[ItemStack]]] = gachaPrizesListReference.get

          override def drawGacha(player: Player, draws: Int): F[Unit] =
            new BukkitDrawGacha[F].draw(player, draws)
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
