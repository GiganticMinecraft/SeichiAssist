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
      sql"SELECT do_break_suppression_due_to_mana FROM player_break_suppression_preference WHERE uuid = ${key.toString}"
        .map(rs => rs.boolean("do_break_suppression_due_to_mana"))
        .single()
    }

    doBreakSuppression.map(BreakSuppressionPreference)
  }

  override def write(key: UUID, value: BreakSuppressionPreference): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val uuid = key.toString
      sql"""INSERT INTO player_break_suppression_preference  (uuid, do_break_suppression_due_to_mana)
           | VALUES ($uuid, ${value.doBreakSuppression})
           | ON DUPLICATE KEY UPDATE
           | do_break_suppression_due_to_mana  = VALUES(do_break_suppression_due_to_mana)
      """.stripMargin.update()
    }
  }
}
