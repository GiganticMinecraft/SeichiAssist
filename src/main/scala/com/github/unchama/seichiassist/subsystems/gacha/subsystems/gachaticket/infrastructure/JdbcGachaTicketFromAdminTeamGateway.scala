package com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.infrastructure

import cats.effect.Sync
import com.github.unchama.concurrent.NonServerThreadContextShift
import com.github.unchama.seichiassist.subsystems.gacha.subsystems.gachaticket.domain.GachaTicketFromAdminTeamGateway
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcGachaTicketFromAdminTeamGateway[F[_]: Sync: NonServerThreadContextShift]
    extends GachaTicketFromAdminTeamGateway[F] {

  import cats.implicits._

  /**
   * 運営からのガチャ券の枚数を全員に増加させる作用
   */
  override def add(amount: Int): F[Boolean] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        sql"update playerdata set numofsorryforbug = numofsorryforbug + $amount"
          .execute()
          .apply()
      }
    }
  }

  /**
   * 運営からのガチャ券の枚数を指定UUIDのプレイヤーに増加させる作用
   */
  override def add(amount: Int, uuid: UUID): F[Boolean] = {
    NonServerThreadContextShift[F].shift >> Sync[F].delay {
      DB.localTx { implicit session =>
        sql"update playerdata set numofsorryforbug = numofsorryforbug + $amount where uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }
  }

}
