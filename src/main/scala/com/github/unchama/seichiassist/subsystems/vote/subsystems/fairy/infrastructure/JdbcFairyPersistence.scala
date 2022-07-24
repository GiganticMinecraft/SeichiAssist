package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  AppleOpenState,
  FairyPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcFairyPersistence[F[_]: Sync] extends FairyPersistence[F] {

  /**
   * 妖精に開放するりんごの状態を変更する
   */
  override def changeAppleOpenState(uuid: UUID, openState: AppleOpenState): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET toggleGiveApple = ${openState.amount} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精に開放するりんごの状態を取得する
   */
  override def appleOpenState(uuid: UUID): F[AppleOpenState] =
    Sync[F].delay {
      val appleAmount = DB.readOnly { implicit session =>
        sql"SELECT toggleGiveApple FROM playerdata WHERE uuid = ${uuid.toString}"
          .map(_.int("toggleGiveApple"))
          .single()
          .apply()
          .get
      }
      AppleOpenState.values.find(_.amount == appleAmount).get
    }
}
