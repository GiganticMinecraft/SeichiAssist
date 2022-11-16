package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.{
  RegionNumber,
  RegionNumberPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcRegionNumberPersistence[F[_]: Sync] extends RegionNumberPersistence[F] {

  override def setRegionNumber(uuid: UUID, regionNumber: RegionNumber): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET rgnum = ${regionNumber.value} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  override def fetchRegionNumber(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT rgnum FROM playerdata WHERE uuid = ${uuid.toString}".execute().apply()
    }
  }

}
