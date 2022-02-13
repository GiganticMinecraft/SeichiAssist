package com.github.unchama.seichiassist.subsystems.seasonalevents.infrastructure

import java.time.LocalDateTime
import java.util.UUID

import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import cats.effect.Sync
import com.github.unchama.seichiassist.database.DatabaseConstants
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

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