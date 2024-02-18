package com.github.unchama.seichiassist.subsystems.gridregion.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gridregion.domain.RegionCount
import com.github.unchama.seichiassist.subsystems.gridregion.domain.persistence.RegionCountPersistence
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcRegionCountPersistence[F[_]: Sync] extends RegionCountPersistence[F] {

  override def write(uuid: UUID, regionCount: RegionCount): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET rgnum = ${regionCount.value} WHERE uuid = ${uuid.toString}"
        .execute()
    }
  }

  override def read(uuid: UUID): F[Option[RegionCount]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT rgnum FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.int("rgnum"))
        .single()
        .map(num => RegionCount(num))
    }
  }

}
