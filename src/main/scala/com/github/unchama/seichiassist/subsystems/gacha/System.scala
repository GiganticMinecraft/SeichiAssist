package com.github.unchama.seichiassist.subsystems.gacha

import cats.Functor
import cats.data.Kleisli
import cats.effect.concurrent.Ref
import cats.effect.{ConcurrentEffect, SyncEffect}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.datarepository.bukkit.player.BukkitRepositoryControls
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.serialization.SerializeAndDeserialize
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.minecraft.bukkit.algebra.BukkitItemStackSerializeAndDeserialize
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.application.actions.GrantGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.application.repository.GachaSettingRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.BukkitItemStackCanBeSignedAsGachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.{
  BukkitDrawGacha,
  BukkitGrantGachaPrize
}
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.factories.BukkitStaticGachaPrizeFactory
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain._
import com.github.unchama.seichiassist.subsystems.gacha.domain.gachaprize.{
  GachaPrize,
  GachaPrizeId
}
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.JdbcGachaPrizeListPersistence
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.GachaTicketAPI
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

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect, G[
    _
  ]: SyncEffect: ContextCoercion[*[_], F]](
    implicit gachaTicketAPI: GachaTicketAPI[F]
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

    val system: F[System[F]] = {
      for {
        gachaPrizesListReference <- Ref.of[F, Vector[GachaPrize[ItemStack]]](Vector.empty)
        gachaDrawSettingRepositoryControls <- ContextCoercion(
          BukkitRepositoryControls.createHandles(
            RepositoryDefinition
              .Phased
              .TwoPhased(
                GachaSettingRepositoryDefinition.initialization[G, Player],
                GachaSettingRepositoryDefinition.finalization[G, Player]
              )
          )
        )
      } yield {
        new System[F] {
          override implicit val api: GachaAPI[F, ItemStack, Player] =
            new GachaAPI[F, ItemStack, Player] {
              val gachaDrawSettingRepository = gachaDrawSettingRepositoryControls.repository
              override protected implicit val F: Functor[F] = implicitly

              override def load: F[Unit] = _gachaPersistence.list.flatMap { gachaPrizes =>
                gachaPrizesListReference.set(gachaPrizes)
              }

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

              override def canBeSignedAsGachaPrize: CanBeSignedAsGachaPrize[ItemStack] =
                _canBeSignedAsGachaPrize

              override def toggleConsumeGachaTicketAmount: Kleisli[F, Player, Unit] = Kleisli {
                player =>
                  ContextCoercion(
                    gachaDrawSettingRepository(player).toggleConsumeGachaTicketAmount()
                  )
              }

              override def consumeGachaTicketAmount(player: Player): F[GachaTicketConsumeAmount] =
                ContextCoercion(gachaDrawSettingRepository(player).consumeGachaTicketAmount())
            }
          override val commands: Map[String, TabExecutor] = Map(
            "gacha" -> new GachaCommand[F]().executor
          )
          override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F]())

          override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
            Seq(gachaDrawSettingRepositoryControls).map(_.coerceFinalizationContextTo[F])
        }
      }
    }

    for {
      system <- system
      _ <- system.api.load
    } yield system
  }

}
