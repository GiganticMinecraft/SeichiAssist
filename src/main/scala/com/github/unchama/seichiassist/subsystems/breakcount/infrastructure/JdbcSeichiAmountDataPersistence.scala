package com.github.unchama.seichiassist.subsystems.breakcount.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakcount.domain.level.SeichiExpAmount
import com.github.unchama.seichiassist.subsystems.breakcount.domain.{
  SeichiAmountData,
  SeichiAmountDataPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcSeichiAmountDataPersistence[F[_]](implicit F: Sync[F])
    extends SeichiAmountDataPersistence[F] {

  override def read(key: UUID): F[Option[SeichiAmountData]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"select totalbreaknum from playerdata where uuid = ${key.toString}"
          .map { rs =>
            SeichiAmountData(
              SeichiExpAmount.ofNonNegative(rs.bigInt("totalbreaknum").longValueExact())
            )
          }
          .first()
          .apply()
      }
    }

  override def write(key: UUID, value: SeichiAmountData): F[Unit] =
    F.delay {
      DB.localTx { implicit session =>
        sql"update playerdata set totalbreaknum = ${value.expAmount.amount} where uuid = ${key.toString}"
          .update()
          .apply()
      }
    }

}
