package com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.persistence

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breaksuppressionpreference.domain.{
  BreakSuppressionPreference,
  BreakSuppressionPreferencePersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakSuppressionPreferencePersistence[F[_]: Sync]
    extends BreakSuppressionPreferencePersistence[F] {

  override def read(key: UUID): F[Option[BreakSuppressionPreference]] = Sync[F].delay {
    val doBreakSuppression = DB.readOnly { implicit session =>
      sql"SELECT do_break_suppression FROM player_break_suppression_preference WHERE uuid = ${key.toString}"
        .map(rs => rs.boolean("do_break_suppression"))
        .single()
    }

    doBreakSuppression.map(BreakSuppressionPreference)
  }

  override def write(key: UUID, value: BreakSuppressionPreference): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val uuid = key.toString
      sql"""INSERT INTO player_break_suppression_preference  (uuid, player_break_suppression_preference)
           | VALUES ($uuid, ${value.doBreakSuppression})
           | ON DUPLICATE KEY UPDATE
           | do_break_suppression  = VALUE(do_break_suppression)
      """.stripMargin.update().apply()
    }
  }
}
