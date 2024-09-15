package com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.persistence

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakskilltriggerconfig.domain.{
  BreakSkillTriggerConfig,
  BreakSkillTriggerConfigKey,
  BreakSkillTriggerConfigPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakSkillTriggerConfigPersistence[F[_]: Sync]
    extends BreakSkillTriggerConfigPersistence[F] {

  override def read(key: UUID): F[Option[BreakSkillTriggerConfig]] = Sync[F].delay {
    val config = DB.readOnly { implicit session =>
      sql"SELECT trigger_category, do_not_break FROM player_break_trigger_preference WHERE uuid = ${key.toString}"
        .map { rs =>
          BreakSkillTriggerConfigKey.withNameOption(rs.string("trigger_category")).map {
            flagName => flagName -> rs.boolean("do_not_break")
          }
        }
        .toList()
        .flatten
        .toMap
    }

    Some(BreakSkillTriggerConfig(config))
  }

  override def write(key: UUID, value: BreakSkillTriggerConfig): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      val uuid = key.toString
      val batchParams =
        value
          .config
          .map {
            case (triggerCategory, doNotBreak) =>
              Seq(uuid, triggerCategory.entryName, doNotBreak)
          }
          .toSeq

      sql"""INSERT INTO player_break_trigger_preference (uuid, trigger_category, do_not_break)
           | VALUES (?, ?, ?)
           | ON DUPLICATE KEY UPDATE
           | do_not_break = VALUE(do_not_break)
                   """.stripMargin.batch(batchParams: _*).apply[List]()
    }
  }

}
