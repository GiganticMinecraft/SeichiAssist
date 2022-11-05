package com.github.unchama.seichiassist.subsystems.minestack.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.minestack.domain.PlayerSettingPersistence
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

}
