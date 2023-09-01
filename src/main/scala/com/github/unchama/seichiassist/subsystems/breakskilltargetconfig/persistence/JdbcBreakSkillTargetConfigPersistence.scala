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
      sql"SELECT block_category, do_break FROM player_break_preference WHERE uuid = ${key.toString}"
        .map { rs =>
          BreakSkillTargetConfigKey.withNameOption(rs.string("block_category")).map {
            flagName => flagName -> rs.boolean("do_break")
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
            case (blockCategory, doBreak) =>
              Seq(uuid, blockCategory.entryName, doBreak)
          }
          .toSeq

      sql"""INSERT INTO player_break_preference (uuid, block_category, do_break)
           | VALUES (?, ?, ?)
           | ON DUPLICATE KEY UPDATE
           | do_break = VALUE(do_break)
                   """.stripMargin.batch(batchParams: _*).apply[List]()
    }
  }

}
