package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState.EnabledWithLimit
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.{
  FastDiggingEffectSuppressionState,
  FastDiggingEffectSuppressionStatePersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcFastDiggingEffectSuppressionStatePersistence[F[_]: Sync]
    extends FastDiggingEffectSuppressionStatePersistence[F] {

  // region コーデック

  private def intToSuppressionState(n: Int): FastDiggingEffectSuppressionState =
    n match {
      case 0 => FastDiggingEffectSuppressionState.EnabledWithoutLimit
      case 1 => FastDiggingEffectSuppressionState.EnabledWithLimit.Of_127
      case 2 => FastDiggingEffectSuppressionState.EnabledWithLimit.Of_200
      case 3 => FastDiggingEffectSuppressionState.EnabledWithLimit.Of_400
      case 4 => FastDiggingEffectSuppressionState.EnabledWithLimit.Of_600
      case _ => FastDiggingEffectSuppressionState.Disabled
    }

  private def suppressionStateToInt(s: FastDiggingEffectSuppressionState): Int =
    s match {
      case FastDiggingEffectSuppressionState.EnabledWithoutLimit => 0
      case limit: FastDiggingEffectSuppressionState.EnabledWithLimit =>
        limit match {
          case EnabledWithLimit.Of_127 => 1
          case EnabledWithLimit.Of_200 => 2
          case EnabledWithLimit.Of_400 => 3
          case EnabledWithLimit.Of_600 => 4
        }
      case FastDiggingEffectSuppressionState.Disabled => 5
    }

  // endregion

  override def read(key: UUID): F[Option[FastDiggingEffectSuppressionState]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"select effectflag from playerdata where uuid = ${key.toString}"
        .map { rs => intToSuppressionState(rs.int("effectflag")) }
        .headOption()
    }
  }

  override def write(key: UUID, value: FastDiggingEffectSuppressionState): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        val encoded = suppressionStateToInt(value)

        sql"update playerdata set effectflag = $encoded where uuid = ${key.toString}".update()
      }
    }

}
