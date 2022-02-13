package com.github.unchama.seichiassist.subsystems.seasonalevents.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.database.DatabaseConstants
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class JdbcLastQuitPersistenceRepository[F[_]](implicit SyncContext: Sync[F]) extends LastQuitPersistenceRepository[F, UUID] {
  override def loadPlayerLastQuit(key: UUID): F[Option[LocalDateTime]] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"select lastquit from ${DatabaseConstants.PLAYERDATA_TABLENAME} where uuid = '${key.toString}'"
          .map { rs => rs.string("lastquit") }.first().apply()
          .map(LocalDateTime.parse(_, DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")))
      }
    }
  }
}