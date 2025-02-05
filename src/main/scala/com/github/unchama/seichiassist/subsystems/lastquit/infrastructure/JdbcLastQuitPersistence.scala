package com.github.unchama.seichiassist.subsystems.lastquit.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.lastquit.domain.{
  LastQuitDateTime,
  LastQuitPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcLastQuitPersistence[F[_]: Sync] extends LastQuitPersistence[F] {

  override def updateLastQuitNow(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET lastquit = NOW() WHERE uuid = ${uuid.toString}".execute()
    }
  }

  override def lastQuitDateTime(uuid: UUID): F[Option[LastQuitDateTime]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val lastQuitDateTime =
          sql"SELECT lastquit FROM playerdata WHERE uuid = ${uuid.toString}"
            .map(_.localDateTime("lastquit"))
            .toList()
            .headOption
        lastQuitDateTime.map(LastQuitDateTime)
      }
    }
}
