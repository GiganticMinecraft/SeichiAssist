package com.github.unchama.seichiassist.subsystems.breakcountbar.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.breakcountbar.domain.{BreakCountBarVisibility, BreakCountBarVisibilityPersistence}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcBreakCountBarVisibilityPersistence[F[_]](implicit F: Sync[F])
  extends BreakCountBarVisibilityPersistence[F] {

  override def read(key: UUID): F[Option[BreakCountBarVisibility]] =
    F.delay {
      DB.localTx { implicit session =>
        sql"select extended_barstyle from playerdata where uuid = ${key.toString}"
          .map { rs =>
            rs.string("extended_barstyle") match {
              case "break" => BreakCountBarVisibility.ShownSeichiBreakAmount
              case "build" => BreakCountBarVisibility.ShownBuildAmount
              case "berserk" => BreakCountBarVisibility.ShownGiganticBerserkAmount
              case "none" => BreakCountBarVisibility.Hidden
              case _ => throw new IllegalStateException("expected one of break, build, berserk, or none")
            }
          }
          .first().apply()
      }
    }

  override def write(key: UUID, value: BreakCountBarVisibility): F[Unit] =
    F.delay {
      DB.localTx { implicit session =>
        val show = value match {
          case BreakCountBarVisibility.ShownSeichiBreakAmount => "break"
          case BreakCountBarVisibility.ShownBuildAmount => "build"
          case BreakCountBarVisibility.ShownGiganticBerserkAmount => "berserk"
          case BreakCountBarVisibility.Hidden => "none"
        }
        sql"update playerdata set extended_barstyle = $show where uuid = ${key.toString}"
          .update().apply()
      }
    }

}
