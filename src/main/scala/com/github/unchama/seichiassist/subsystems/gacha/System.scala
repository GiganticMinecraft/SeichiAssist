package com.github.unchama.seichiassist.subsystems.gacha

import cats.effect.ConcurrentEffect.ops.toAllConcurrentEffectOps
import cats.effect.{ConcurrentEffect, SyncIO}
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.minecraft.actions.OnMinecraftServerThread
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.actions.BukkitDrawGacha
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.command.GachaCommand
import com.github.unchama.seichiassist.subsystems.gacha.bukkit.listeners.PlayerPullGachaListener
import com.github.unchama.seichiassist.subsystems.gacha.domain.GachaPrizesDataOperations
import com.github.unchama.seichiassist.subsystems.gacha.domain.bukkit.GachaPrize
import com.github.unchama.seichiassist.subsystems.gacha.infrastructure.bukkit.JdbcGachaPersistence
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamGateway
import com.github.unchama.seichiassist.subsystems.itemmigration.domain.minecraft.UuidRepository
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import org.bukkit.event.Listener

trait System[F[_]] extends Subsystem[F] {
  val api: GachaReadAPI[F, Player] with GachaWriteAPI[F]
}

object System {

  def wired[F[_]: OnMinecraftServerThread: NonServerThreadContextShift: ConcurrentEffect](
    implicit syncUuidRepository: UuidRepository[SyncIO],
    gachaPrizesDataOperations: GachaPrizesDataOperations[F]
  ): System[F] = {
    implicit val gachaPersistence: JdbcGachaPersistence[F] = new JdbcGachaPersistence[F]()
    implicit val gachaTicketPersistence: JdbcGachaTicketFromAdminTeamGateway[F] =
      new JdbcGachaTicketFromAdminTeamGateway[F]

    new System[F] {
      gachaPrizesDataOperations.loadGachaPrizes(gachaPersistence).toIO.unsafeRunAsyncAndForget()

      override implicit val api: GachaAPI[F, Player] = new GachaAPI[F, Player] {

        override def list: F[Vector[GachaPrize]] = gachaPersistence.list

        override def replace(gachaPrizesList: Vector[GachaPrize]): F[Unit] =
          gachaPersistence.update(gachaPrizesList)

        override def pull(player: Player, amount: Int): F[Unit] =
          BukkitDrawGacha[F].draw(player, amount)
      }
      override val commands: Map[String, TabExecutor] = Map(
        "gacha" -> new GachaCommand[F]().executor
      )
      override val listeners: Seq[Listener] = Seq(new PlayerPullGachaListener[F]())
    }
  }

}
