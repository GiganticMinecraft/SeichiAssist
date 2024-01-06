package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.{
  GachaTicketAmount,
  GachaTicketFromAdminTeamRepository,
  GrantResultOfGachaTicketFromAdminTeam
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcGachaTicketFromAdminTeamRepository[F[_]: Sync: NonServerThreadContextShift]
    extends GachaTicketFromAdminTeamRepository[F] {

  import cats.implicits._

  /**
   * @return 呼び出された時点で永続化バックエンド中にある全プレイヤーの「運営からのガチャ券」を増加させる作用
   */
  override def addToAllKnownPlayers(amount: GachaTicketAmount): F[Unit] = {
    // NOTE: apply関数はBooleanを返すのでdelayメソッドには型明示が必要
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        sql"update playerdata set numofsorryforbug = numofsorryforbug + ${amount.value}"
          .execute()
      }
    }
  }

  /**
   * @return 指定されたプレイヤー名の「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addByPlayerName(
    amount: GachaTicketAmount,
    playerName: PlayerName
  ): F[GrantResultOfGachaTicketFromAdminTeam] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        val affectedRows =
          sql"UPDATE playerdata SET numofsorryforbug = numofsorryforbug + ${amount.value} WHERE name = ${playerName.name}"
            .update()

        getReceiptResult(affectedRows)
      }
    }
  }

  /**
   *  @return 指定されたUUIDの「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addByUUID(
    amount: GachaTicketAmount,
    uuid: UUID
  ): F[GrantResultOfGachaTicketFromAdminTeam] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        val affectedRows =
          sql"UPDATE playerdata SET numofsorryforbug = numofsorryforbug + ${amount.value} WHERE uuid = ${uuid.toString}"
            .update()

        getReceiptResult(affectedRows)
      }
    }
  }

  override def receive(uuid: UUID): F[GachaTicketAmount] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        val hasAmount =
          sql"SELECT numofsorryforbug FROM playerdata WHERE uuid = ${uuid.toString} FOR UPDATE"
            .map(_.int("numofsorryforbug"))
            .toList()
            .headOption
            .getOrElse(0)

        val nineStackAmount = 576
        val receiveAmount = Math.min(hasAmount, nineStackAmount)
        val updatedAmount = hasAmount - receiveAmount

        if (updatedAmount >= 0) {
          sql"UPDATE playerdata SET numofsorryforbug = numofsorryforbug - $receiveAmount WHERE uuid = ${uuid.toString}"
            .execute()
        }

        GachaTicketAmount(receiveAmount)
      }
    }
  }

  private def getReceiptResult(updatedRows: Int): GrantResultOfGachaTicketFromAdminTeam =
    updatedRows match {
      case 0 => GrantResultOfGachaTicketFromAdminTeam.NotExists
      case 1 => GrantResultOfGachaTicketFromAdminTeam.Success
      case _ => GrantResultOfGachaTicketFromAdminTeam.GrantedToMultiplePlayers
    }

}
