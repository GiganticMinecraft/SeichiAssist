package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.meta.subsystem.Subsystem
import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.{
  GachaTicketAmount,
  GachaTicketFromAdminTeamRepository,
  GrantResultOfGachaTicketFromAdminTeam
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure.JdbcGachaTicketFromAdminTeamRepository

import java.util.UUID

trait System[F[_]] extends Subsystem[F] {

  val api: GachaTicketAPI[F]

}

object System {

  def wired[F[_]: Sync: NonServerThreadContextShift]: System[F] = {
    val gachaTicketFromAdminTeamRepository: GachaTicketFromAdminTeamRepository[F] =
      new JdbcGachaTicketFromAdminTeamRepository[F]

    new System[F] {
      override val api: GachaTicketAPI[F] = new GachaTicketAPI[F] {

        override def addToAllKnownPlayers(amount: GachaTicketAmount): F[Unit] =
          gachaTicketFromAdminTeamRepository.addToAllKnownPlayers(amount)

        override def addByPlayerName(
          amount: GachaTicketAmount,
          playerName: PlayerName
        ): F[GrantResultOfGachaTicketFromAdminTeam] =
          gachaTicketFromAdminTeamRepository.addByPlayerName(amount, playerName)

        override def addByUUID(
          amount: GachaTicketAmount,
          uuid: UUID
        ): F[GrantResultOfGachaTicketFromAdminTeam] =
          gachaTicketFromAdminTeamRepository.addByUUID(amount, uuid)

        override def receive(uuid: UUID): F[GachaTicketAmount] =
          gachaTicketFromAdminTeamRepository.receive(uuid)
      }
    }
  }

}
