package com.github.unchama.seichiassist.subsystems.buildcount.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.BuildAmountRateLimiterSnapshot
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.BuildAmountRateLimitPersistence
import scalikejdbc._

import java.util.UUID

class JdbcBuildAmountRateLimitPersistence[SyncContext[_]](
  implicit SyncContext: Sync[SyncContext],
  config: Configuration
) extends BuildAmountRateLimitPersistence[SyncContext] {

  override def read(key: UUID): SyncContext[Option[BuildAmountRateLimiterSnapshot]] =
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"select available_permission, record_date from build_count_rate_limit where uuid = ${key.toString}"
          .stripMargin
          .map { rs =>
            val exp = BuildExpAmount(rs.bigDecimal("available_permission"))
            val ldt = rs.localDateTime("record_date")

            BuildAmountRateLimiterSnapshot(exp, ldt)
          }
          .first()
          .apply()
      }
    }

  override def write(key: UUID, value: BuildAmountRateLimiterSnapshot): SyncContext[Unit] =
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"""
             |insert into build_count_rate_limit values (${key.toString}, ${value
              .amount
              .toPlainString}, ${value.recordTime})
             |  on duplicate key update
             |    available_permission = ${value.amount.toPlainString},
             |    record_date = ${value.recordTime}
             |""".stripMargin.update().apply()
      }
    }
}
