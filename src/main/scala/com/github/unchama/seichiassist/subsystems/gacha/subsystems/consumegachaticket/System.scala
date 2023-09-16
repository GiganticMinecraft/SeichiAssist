package com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket

import cats.data.Kleisli
import cats.effect.{Sync, SyncEffect}
import com.github.unchama.datarepository.bukkit.player.{
  BukkitRepositoryControls,
  PlayerDataRepository
}
import com.github.unchama.datarepository.template.RepositoryDefinition
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.application.repository.ConsumeGachaTicketSettingRepositoryDefinition
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.consumegachaticket.domain.{
  ConsumeGachaTicketSettings,
  GachaTicketConsumeAmount
}
import org.bukkit.entity.Player

trait System[F[_]] extends Subsystem[F] {

  val api: ConsumeGachaTicketAPI[F, Player]

}

object System {

  import cats.implicits._

  def wired[F[_]: Sync, G[_]: SyncEffect: ContextCoercion[*[_], F]]: F[System[F]] = {
    for {
      consumeGachaTicketSettingRepositoryControls <- ContextCoercion(
        BukkitRepositoryControls.createHandles(
          RepositoryDefinition
            .Phased
            .TwoPhased(
              ConsumeGachaTicketSettingRepositoryDefinition.initialization[G, Player],
              ConsumeGachaTicketSettingRepositoryDefinition.finalization[G, Player]
            )
        )
      )
    } yield {
      new System[F] {

        override implicit val api: ConsumeGachaTicketAPI[F, Player] =
          new ConsumeGachaTicketAPI[F, Player] {
            val consumeGachaTicketSettingRepository
              : PlayerDataRepository[ConsumeGachaTicketSettings[G]] =
              consumeGachaTicketSettingRepositoryControls.repository

            override def toggleConsumeGachaTicketAmount: Kleisli[F, Player, Unit] = Kleisli {
              player =>
                ContextCoercion(
                  consumeGachaTicketSettingRepository(player).toggleConsumeGachaTicketAmount()
                )
            }

            override def consumeGachaTicketAmount(player: Player): F[GachaTicketConsumeAmount] =
              ContextCoercion(
                consumeGachaTicketSettingRepository(player).consumeGachaTicketAmount()
              )

          }
        override val managedRepositoryControls: Seq[BukkitRepositoryControls[F, _]] =
          Seq(consumeGachaTicketSettingRepositoryControls).map(_.coerceFinalizationContextTo[F])

      }
    }
  }
}
