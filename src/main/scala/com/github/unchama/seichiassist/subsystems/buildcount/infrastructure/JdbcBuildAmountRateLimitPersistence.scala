package com.github.unchama.seichiassist.subsystems.buildcount.infrastructure

import cats.effect.{ConcurrentEffect, Sync, Timer}
import cats.implicits._
import com.github.unchama.generic.ContextCoercion
import com.github.unchama.generic.algebra.typeclasses.OrderedMonus
import com.github.unchama.generic.ratelimiting.{FixedWindowRateLimiter, RateLimiter}
import com.github.unchama.seichiassist.subsystems.buildcount.application.Configuration
import com.github.unchama.seichiassist.subsystems.buildcount.domain.BuildAmountPermission
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.{BuildAmountData, BuildAmountDataPersistence, BuildAmountRateLimitPersistence}
import scalikejdbc._

import java.util.UUID

class JdbcBuildAmountRateLimitPersistence[
  SyncContext[_]: ContextCoercion[*[_], ConcurrentContext],
  ConcurrentContext[_]: ConcurrentEffect: Timer
](implicit F: Sync[SyncContext], config: Configuration)
  extends BuildAmountRateLimitPersistence[SyncContext] {

  override def read(key: UUID): SyncContext[Option[BuildAmountPermission]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"select available_permission from build_count_rate_limit where uuid = ${key.toString}"
          .stripMargin
          .map { rs =>
            val exp = BuildExpAmount(rs.bigDecimal("available_permission"))

            BuildAmountPermission(exp)
          }
          .first().apply()
      }
    }

  override def write(key: UUID, value: BuildAmountPermission): SyncContext[Unit] = F.delay {
    DB.localTx { implicit session =>
      sql"update build_count_rate_limit set available_permission = ${value.raw.toPlainString} where uuid = ${key.toString}"
        .stripMargin
        .update().apply()
    }
  }
}
