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
        sql"select build_count from rate_limit where uuid = ${key.toString} and rate_limit_name = 'build'"
          .stripMargin
          .map { rs =>
            val exp = BuildExpAmount(BigDecimal(rs.string("current_value")))

            BuildAmountPermission(exp)
          }
          .first().apply()
      }
    }

  override def write(key: UUID, value: BuildAmountPermission): SyncContext[Unit] = F.delay {
    DB.localTx { implicit session =>
      sql"update playerdata set build_count = $value where uuid = ${key.toString} and rate_limit_name = 'build'"
        .stripMargin
        .update().apply()
    }
  }
}
