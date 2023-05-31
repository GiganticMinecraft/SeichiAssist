package com.github.unchama.seichiassist.subsystems.breakflags.persistence

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakflags.domain.{
  BreakFlag,
  BreakFlagName,
  BreakFlagPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakFlagPersistence[F[_]: Sync] extends BreakFlagPersistence[F] {
  override def read(key: UUID): F[Option[List[BreakFlag]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val breakFlags =
        sql"SELECT flag_name, can_break FROM break_flags WHERE uuid = ${key.toString}"
          .map { rs =>
            BreakFlagName.withNameOption(rs.string("flag_name")).map { flagName =>
              BreakFlag(flagName, rs.boolean("can_break"))
            }
          }
          .toList()
          .apply()
          .collect { case Some(flag) => flag }

      Some(breakFlags)
    }
  }

  override def write(key: UUID, value: List[BreakFlag]): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val uuid = key.toString
      val batchParams = value.map { flag => Seq(uuid, flag.flagName.entryName, flag.flag) }

      sql"""INSERT INTO break_flags (uuid, flag_name, can_break)
           | VALUES (?, ?, ?)
           | ON DUPLICATE KEY UPDATE
           | can_break = VALUE(can_break)
         """.stripMargin.batch(batchParams: _*).apply[List]
    }
  }

}
