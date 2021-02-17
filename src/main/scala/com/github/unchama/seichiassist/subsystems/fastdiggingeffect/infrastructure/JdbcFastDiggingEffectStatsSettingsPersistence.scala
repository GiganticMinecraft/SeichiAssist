package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{FastDiggingEffectStatsSettings, FastDiggingEffectStatsSettingsPersistence}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcFastDiggingEffectStatsSettingsPersistence[F[_] : Sync]
  extends FastDiggingEffectStatsSettingsPersistence[F] {

  //region コーデック

  private def booleanToSettings(b: Boolean): FastDiggingEffectStatsSettings =
    if (b)
      FastDiggingEffectStatsSettings.Receive
    else
      FastDiggingEffectStatsSettings.Mute

  private def settingsToBoolean(s: FastDiggingEffectStatsSettings): Boolean =
    s match {
      case FastDiggingEffectStatsSettings.Receive => true
      case FastDiggingEffectStatsSettings.Mute => false
    }

  //endregion

  override def read(key: UUID): F[Option[FastDiggingEffectStatsSettings]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"select messageflag from playerdata where uuid = ${key.toString}"
        .map { rs => booleanToSettings(rs.boolean("effectflag")) }
        .headOption()
        .apply()
    }
  }

  override def write(key: UUID, value: FastDiggingEffectStatsSettings): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val encoded = settingsToBoolean(value)

      sql"update playerdata set messageflag = $encoded where uuid = ${key.toString}".update().apply()
    }
  }
}
