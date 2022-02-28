package com.github.unchama.seichiassist.subsystems.buildcount.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.buildcount.domain.explevel.BuildExpAmount
import com.github.unchama.seichiassist.subsystems.buildcount.domain.playerdata.{
  BuildAmountData,
  BuildAmountDataPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBuildAmountDataPersistence[F[_]](implicit F: Sync[F])
    extends BuildAmountDataPersistence[F] {

  override def read(key: UUID): F[Option[BuildAmountData]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"select build_count from playerdata where uuid = ${key.toString}"
          .stripMargin
          .map { rs =>
            val exp = BuildExpAmount(BigDecimal(rs.string("build_count")))

            BuildAmountData(exp)
          }
          .first()
          .apply()
      }
    }

  override def write(key: UUID, value: BuildAmountData): F[Unit] =
    F.delay {
      DB.localTx { implicit session =>
        sql"update playerdata set build_count = ${value.expAmount.amount} where uuid = ${key.toString}"
          .stripMargin
          .update()
          .apply()
      }
    }

}
