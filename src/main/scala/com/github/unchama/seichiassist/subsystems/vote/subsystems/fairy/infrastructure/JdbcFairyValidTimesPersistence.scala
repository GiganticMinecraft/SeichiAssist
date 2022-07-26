package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  FairyValidTimes,
  FairyValidTimesPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.time.{LocalDateTime, ZoneId, ZonedDateTime}
import java.util.{Date, UUID}

class JdbcFairyValidTimesPersistence[F[_]: Sync] extends FairyValidTimesPersistence[F] {
  override def read(uuid: UUID): F[Option[FairyValidTimes]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val dateOpt = sql"SELECT newVotingFairyTime FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.date("newVotingFairyTime"))
        .single()
        .apply()
      dateOpt.map { date =>
        FairyValidTimes(Some(LocalDateTime.ofInstant(date.toInstant, ZoneId.systemDefault())))
      }
    }
  }

  /**
   * [[FairyValidTimes]]を[[java.util.Date]]に変換してDBに保存する
   */
  override def write(uuid: UUID, fairyValidTimes: FairyValidTimes): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET newVotingFairyTime = ${Date.from(
            ZonedDateTime.of(fairyValidTimes.endTimeOpt.get, ZoneId.systemDefault()).toInstant
          )} WHERE uuid = ${uuid.toString}".execute().apply()
      }
    }
}
