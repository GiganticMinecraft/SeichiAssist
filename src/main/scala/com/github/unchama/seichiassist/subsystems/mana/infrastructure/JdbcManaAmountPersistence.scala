package com.github.unchama.seichiassist.subsystems.mana.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.mana.domain.{
  ManaAmount,
  ManaAmountPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcManaAmountPersistence[F[_]](implicit F: Sync[F]) extends ManaAmountPersistence[F] {
  override def read(key: UUID): F[Option[ManaAmount]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"select mana from playerdata where uuid = ${key.toString}"
          .map { rs => ManaAmount(rs.double("mana")) }
          .first()
      }
    }

  override def write(key: UUID, value: ManaAmount): F[Unit] =
    F.delay {
      DB.localTx { implicit session =>
        sql"update playerdata set mana = ${value.value} where uuid = ${key.toString}".update()
      }
    }

}
