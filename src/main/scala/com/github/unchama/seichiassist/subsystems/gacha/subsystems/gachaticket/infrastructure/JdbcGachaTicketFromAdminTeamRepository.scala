package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  PlayerName,
  ReceiptResultOfGachaTicketFromAdminTeam
}
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketFromAdminTeamRepository
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcGachaTicketFromAdminTeamRepository[F[_]: Sync: NonServerThreadContextShift]
    extends GachaTicketFromAdminTeamRepository[F] {

  import cats.implicits._

  /**
   * @return 呼び出された時点で永続化バックエンド中にある全プレイヤーの「運営からのガチャ券」を増加させる作用
   */
  override def addToAllKnownPlayers(amount: Int): F[Unit] = {
    // NOTE: apply関数はBooleanを返すのでdelayメソッドには型明示が必要
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        sql"update playerdata set numofsorryforbug = numofsorryforbug + $amount"
          .execute()
          .apply()
      }
    }
  }

  /**
   * @return 指定されたプレイヤー名の「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addByPlayerName(
    amount: Int,
    playerName: PlayerName
  ): F[ReceiptResultOfGachaTicketFromAdminTeam] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        val affectedRows =
          sql"UPDATE playerdata SET numofsorryforbug = numofsorryforbug + $amount WHERE name = ${playerName.name}"
            .update
            .apply()

        getReceiptResult(affectedRows)
      }
    }
  }

  /**
   *  @return 指定されたUUIDの「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addByUUID(
    amount: Int,
    uuid: UUID
  ): F[ReceiptResultOfGachaTicketFromAdminTeam] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        val affectedRows =
          sql"UPDATE playerdata SET numofsorryforbug = numofsorryforbug + $amount WHERE uuid = ${uuid.toString}"
            .update()
            .apply()

        getReceiptResult(affectedRows)
      }
    }
  }

  private def getReceiptResult(updatedRows: Int): ReceiptResultOfGachaTicketFromAdminTeam =
    updatedRows match {
      case 0 => ReceiptResultOfGachaTicketFromAdminTeam.NotExists
      case 1 => ReceiptResultOfGachaTicketFromAdminTeam.Success
    }

}
