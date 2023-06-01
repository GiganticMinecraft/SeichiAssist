package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.persistence

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
  BreakSkillTargetConfigKey,
  BreakSkillTargetConfigPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakSkillTargetConfigPersistence[F[_]: Sync] extends BreakSkillTargetConfigPersistence[F] {
  override def read(key: UUID): F[Option[Set[BreakSkillTargetConfig]]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val breakFlags =
        sql"SELECT flag_name, include FROM player_break_preference WHERE uuid = ${key.toString}"
          .map { rs =>
            BreakSkillTargetConfigKey.withNameOption(rs.string("flag_name")).map { flagName =>
              BreakSkillTargetConfig(flagName, rs.boolean("include"))
            }
          }
          .toList()
          .apply()
          .collect { case Some(flag) => flag }
          .toSet

      Some(breakFlags)
    }
  }

  override def write(key: UUID, value: Set[BreakSkillTargetConfig]): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val uuid = key.toString
      val batchParams = value.map { flag =>
        Seq(uuid, flag.configKey.entryName, flag.includes)
      }.toSeq

      sql"""INSERT INTO player_break_preference (uuid, flag_name, include)
           | VALUES (?, ?, ?)
           | ON DUPLICATE KEY UPDATE
           | include = VALUE(include)
         """.stripMargin.batch(batchParams: _*).apply[List]()
    }
  }

}
