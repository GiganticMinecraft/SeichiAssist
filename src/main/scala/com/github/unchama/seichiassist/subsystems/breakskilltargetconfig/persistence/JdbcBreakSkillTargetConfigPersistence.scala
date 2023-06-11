package com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.persistence

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakskilltargetconfig.domain.{
  BreakSkillTargetConfig,
  BreakSkillTargetConfigKey,
  BreakSkillTargetConfigPersistence,
  BreakSkillTargetConfigRepository
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakSkillTargetConfigPersistence[F[_]: Sync]
    extends BreakSkillTargetConfigPersistence[F] {

  import cats.implicits._

  override def read(key: UUID): F[Option[BreakSkillTargetConfigRepository[F]]] = {
    for {
      breakSkillTargetConfig <- Sync[F].delay {
        DB.readOnly { implicit session =>
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
        }
      }
      repository = new BreakSkillTargetConfigRepository[F]
      _ <- repository.setConfig(breakSkillTargetConfig)
    } yield Some(repository)
  }

  override def write(key: UUID, value: BreakSkillTargetConfigRepository[F]): F[Unit] = {
    for {
      currentConfig <- value.getConfigAll
      _ <- Sync[F].delay {
        DB.localTx { implicit session =>
          val uuid = key.toString
          val batchParams =
            currentConfig.map { flag =>
              Seq(uuid, flag.configKey.entryName, flag.includes)
            }.toSeq

          sql"""INSERT INTO player_break_preference (uuid, flag_name, include)
               | VALUES (?, ?, ?)
               | ON DUPLICATE KEY UPDATE
               | include = VALUE(include)
             """.stripMargin.batch(batchParams: _*).apply[List]()
        }
      }
    } yield ()
  }

}
