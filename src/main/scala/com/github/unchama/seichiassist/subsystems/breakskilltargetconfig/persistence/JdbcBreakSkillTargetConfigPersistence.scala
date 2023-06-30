package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.persistence

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
  BreakSkillTargetConfigKey,
  BreakSkillTargetConfigPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakSkillTargetConfigPersistence[F[_]: Sync]
    extends BreakSkillTargetConfigPersistence[F] {

  override def read(key: UUID): F[Option[BreakSkillTargetConfig]] = Sync[F].delay {
    val config = DB.readOnly { implicit session =>
      sql"SELECT flag_name, include FROM player_break_preference WHERE uuid = ${key.toString}"
        .map { rs =>
          BreakSkillTargetConfigKey.withNameOption(rs.string("flag_name")).map { flagName =>
            flagName -> rs.boolean("include")
          }
        }
        .toList()
        .apply()
        .flatten
        .toMap
    }

    Some(BreakSkillTargetConfig(config))
  }

  override def write(key: UUID, value: BreakSkillTargetConfig): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val uuid = key.toString
      val batchParams =
        value
          .config
          .map {
            case (configKey, includes) =>
              Seq(uuid, configKey.entryName, includes)
          }
          .toSeq

      sql"""INSERT INTO player_break_preference (uuid, flag_name, include)
           | VALUES (?, ?, ?)
           | ON DUPLICATE KEY UPDATE
           | include = VALUE(include)
                   """.stripMargin.batch(batchParams: _*).apply[List]()
    }
  }

}
