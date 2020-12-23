package com.github.unchama.seichiassist.subsystems.seasonalevents.infrastructure

import java.time.LocalDateTime
import java.util.UUID

import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import cats.effect.Sync
import com.github.unchama.seichiassist.database.DatabaseConstants
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcLastQuitPersistenceRepository[F[_]](implicit SyncContext: Sync[F]) extends LastQuitPersistenceRepository[F, String] {
  override def loadPlayerLastQuit(key: String): F[Option[LocalDateTime]] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        // keyはplayerName
        sql"select lastquit from ${DatabaseConstants.PLAYERDATA_TABLENAME} where name = '$key'"
          .map { rs => rs.timestampOpt("lastquit").map(_.toLocalDateTime) }
          .toList().apply().head
      }
    }
  }
}