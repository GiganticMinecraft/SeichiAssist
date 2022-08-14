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
   * 現在データベース中にある全プレイヤーの「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addToAllKnownPlayers(amount: Int): F[Unit] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        sql"update playerdata set numofsorryforbug = numofsorryforbug + $amount"
          .execute()
          .apply()
      }
    }
  }

  /**
   * 指定されたUUIDのプレイヤーの「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addByPlayerName(
    amount: Int,
    playerName: PlayerName
  ): F[ReceiptResultOfGachaTicketFromAdminTeam] = {
    NonServerThreadContextShift[F].shift >> Sync[F]
      .delay[ReceiptResultOfGachaTicketFromAdminTeam] {
        DB.localTx { implicit session =>
          val affectedRows =
            sql"""UPDATE playerdata SET numofsorryforbug = CASE (SELECT COUNT(*) FROM playerdata WHERE name = ${playerName.name})
                 |	WHEN 1 THEN numofsorryforbug + $amount
                 |  ELSE numofsorryforbug
                 |END""".update.apply()

          getReceiptResult(affectedRows)
        }
      }
  }

  /**
   * 指定されたUUIDの「運営からのガチャ券」の枚数を増加させる作用
   */
  override def addByUUID(
    amount: Int,
    uuid: UUID
  ): F[ReceiptResultOfGachaTicketFromAdminTeam] = {
    NonServerThreadContextShift[F].shift >> Sync[F]
      .delay[ReceiptResultOfGachaTicketFromAdminTeam] {
        DB.localTx { implicit session =>
          val affectedRows =
            sql"""UPDATE playerdata SET numofsorryforbug = CASE (SELECT COUNT(*) FROM playerdata WHERE uuid = ${uuid.toString})
                 |	WHEN 1 THEN numofsorryforbug + $amount
                 |  ELSE numofsorryforbug
                 |END""".stripMargin.update().apply()

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
