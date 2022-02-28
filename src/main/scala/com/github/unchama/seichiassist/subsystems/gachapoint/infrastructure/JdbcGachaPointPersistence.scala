package com.github.unchama.seichiassist.subsystems.gachapoint.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gachapoint.domain.gachapoint.{
  GachaPoint,
  GachaPointPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcGachaPointPersistence[F[_]: Sync] extends GachaPointPersistence[F] {
  private def encode(gachaPoint: GachaPoint): BigInt =
    gachaPoint.exp.amount.toBigInt

  private def decode(value: BigInt): GachaPoint =
    GachaPoint.ofNonNegative(BigDecimal(value))

  override def read(key: UUID): F[Option[GachaPoint]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"select gachapoint from playerdata where uuid = ${key.toString}"
        .map { rs => decode(rs.bigInt("gachapoint")) }
        .headOption()
        .apply()
    }
  }

  override def write(key: UUID, value: GachaPoint): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"update playerdata set gachapoint = ${encode(value)} where uuid = ${key.toString}"
        .update()
        .apply()
    }
  }
}
