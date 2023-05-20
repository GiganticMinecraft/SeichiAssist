package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.persistence.PlayerSettingPersistence
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcPlayerSettingPersistence[F[_]: Sync] extends PlayerSettingPersistence[F] {

  override def autoMineStackState(uuid: UUID): F[Boolean] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT minestackflag FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.boolean("minestackflag"))
        .single()
        .apply()
        .getOrElse(false)
    }
  }

  override def turnOnAutoMineStack(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET minestackflag = true WHERE uuid = ${uuid.toString}"
        .execute()
        .apply()
    }
  }

  override def turnOffAutoMineStack(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET minestackflag = false WHERE uuid = ${uuid.toString}"
        .execute()
        .apply()
    }
  }
}
