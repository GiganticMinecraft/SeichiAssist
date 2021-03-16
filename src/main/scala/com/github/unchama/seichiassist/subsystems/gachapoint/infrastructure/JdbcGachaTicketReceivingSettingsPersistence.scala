package com.github.unchama.seichiassist.subsystems.gachapoint.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.settings.{GachaTicketReceivingSettings, GachaTicketReceivingSettingsPersistence}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcGachaTicketReceivingSettingsPersistence[F[_] : Sync] extends GachaTicketReceivingSettingsPersistence[F] {
  private def encode(gachaTicketReceivingSettings: GachaTicketReceivingSettings): Boolean =
    gachaTicketReceivingSettings == GachaTicketReceivingSettings.EveryMinute

  private def decode(value: Boolean): GachaTicketReceivingSettings =
    if (value) {
      GachaTicketReceivingSettings.EveryMinute
    } else {
      GachaTicketReceivingSettings.Batch
    }

  override def read(key: UUID): F[Option[GachaTicketReceivingSettings]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"select gachaflag from playerdata where uuid = ${key.toString}"
        .map { rs => rs.booleanOpt("gachaflag").map(decode) }
        .headOption()
        .apply().flatten
    }
  }


  override def write(key: UUID, value: GachaTicketReceivingSettings): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"update playerdata set gachaflag = ${encode(value)} where uuid = ${key.toString}"
        .update().apply()
    }
  }
}
