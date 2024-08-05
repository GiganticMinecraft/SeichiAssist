package com.github.unchama.seichiassist.subsystems.seasonalevents.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.seasonalevents.domain.LastQuitPersistenceRepository
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.time.LocalDateTime
import java.util.UUID

class JdbcLastQuitPersistenceRepository[F[_]](implicit SyncContext: Sync[F])
    extends LastQuitPersistenceRepository[F, UUID] {
  override def loadPlayerLastQuit(key: UUID): F[Option[LocalDateTime]] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"select lastquit from playerdata where uuid = {uuid}"
          .bindByName("uuid" -> key.toString)
          .map(_.timestampOpt("lastquit").map(_.toLocalDateTime))
          .single()
          .flatten
      }
    }
  }
}
