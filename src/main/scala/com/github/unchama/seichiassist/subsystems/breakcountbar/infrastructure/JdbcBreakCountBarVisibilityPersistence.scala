package com.github.unchama.seichiassist.subsystems.breakcountbar.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{
  BreakCountBarVisibility,
  BreakCountBarVisibilityPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakCountBarVisibilityPersistence[F[_]](implicit F: Sync[F])
    extends BreakCountBarVisibilityPersistence[F] {

  override def read(key: UUID): F[Option[BreakCountBarVisibility]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"select expvisible from playerdata where uuid = ${key.toString}"
          .map { rs =>
            if (rs.boolean("expvisible")) {
              BreakCountBarVisibility.Shown
            } else {
              BreakCountBarVisibility.Hidden
            }
          }
          .first()
      }
    }

  override def write(key: UUID, value: BreakCountBarVisibility): F[Unit] =
    F.delay {
      DB.localTx { implicit session =>
        sql"update playerdata set expvisible = ${value == BreakCountBarVisibility.Shown} where uuid = ${key.toString}"
          .update()
      }
    }

}
