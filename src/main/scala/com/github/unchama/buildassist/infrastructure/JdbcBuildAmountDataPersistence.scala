package com.github.unchama.buildassist.infrastructure

import cats.effect.Sync
import com.github.unchama.buildassist.domain.explevel.{BuildExpAmount, BuildLevel}
import com.github.unchama.buildassist.domain.playerdata.{BuildAmountData, BuildAmountDataPersistence}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBuildAmountDataPersistence[F[_]](implicit F: Sync[F])
  extends BuildAmountDataPersistence[F] {

  override def read(key: UUID): F[Option[BuildAmountData]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"""select build_lv, build_count from playerdata
             |  where uuid = ${key.toString}"""
          .stripMargin
          .map { rs =>
            val level = BuildLevel.ofPositive(rs.int("build_lv"))
            val exp = BuildExpAmount(BigDecimal(rs.string("build_count")))

            BuildAmountData(exp, level)
          }
          .first().apply()
      }
    }

  override def write(key: UUID, value: BuildAmountData): F[Unit] =
    F.delay {
      DB.localTx { implicit session =>
        sql"""update playerdata set
             |  build_lv = ${value.desyncedLevel.level},
             |  build_count = ${value.expAmount.amount}
             |  where uuid = ${key.toString}
             |""".stripMargin
      }
    }

}
