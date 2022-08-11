package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.domain.PlayerName
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketFromAdminTeamRepository
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

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
  override def add(amount: Int, playerName: PlayerName): F[Unit] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay[Unit] {
      DB.localTx { implicit session =>
        sql"update playerdata set numofsorryforbug = numofsorryforbug + $amount where playername = ${playerName.name}"
          .execute()
          .apply()
      }
    }
  }

}
