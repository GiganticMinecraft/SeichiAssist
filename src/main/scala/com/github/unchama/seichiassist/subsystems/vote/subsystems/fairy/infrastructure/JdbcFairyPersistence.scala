package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.{
  AppleOpenState,
  FairyPersistence,
  FairyRecoveryMana,
  FairyUsingState,
  FairyValidTimeState
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

  /**
   * 妖精が有効な時間の状態を更新します
   */
  override def updateFairyValidTimeState(
    uuid: UUID,
    fairyValidTimeState: FairyValidTimeState
  ): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET toggleVotingFairy = ${fairyValidTimeState.value} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精が有効な時間の状態を取得します
   */
  override def fairySummonState(uuid: UUID): F[FairyValidTimeState] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val validTimeState =
        sql"SELECT toggleVotingFairy FROM playerdata WHERE uuid = ${uuid.toString}"
          .map(_.int("toggleVotingFairy"))
          .single()
          .apply()
          .get
      FairyValidTimeState(validTimeState)
    }
  }

  /**
   * 妖精が召喚されているかを更新します
   */
  override def updateFairyUsingState(uuid: UUID, fairyUsingState: FairyUsingState): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""UPDATE playerdata 
             | SET canVotingFairyUse = ${if (fairyUsingState == FairyUsingState.Using) true
            else false} WHERE uuid = ${uuid.toString}""".stripMargin.execute().apply()
      }
    }

  /**
   * 妖精が召喚されているかを取得します
   */
  override def fairyUsingState(uuid: UUID): F[FairyUsingState] = Sync[F].delay {
    val isFairyUsing = DB.readOnly { implicit session =>
      sql"SELECT canVotingFairyUse FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.boolean("canVotingFairyUse"))
        .single()
        .apply()
    }.get
    if (isFairyUsing) FairyUsingState.Using
    else FairyUsingState.NotUsing
  }

  /**
   * 妖精が回復するマナの量を変更する
   */
  override def updateFairyRecoveryMana(
    uuid: UUID,
    fairyRecoveryMana: FairyRecoveryMana
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE playerdata SET VotingFairyRecoveryValue = ${fairyRecoveryMana.recoveryMana} WHERE uuid = $uuid"
        .execute()
        .apply()
    }
  }

  /**
   * 妖精が回復するマナの量を取得する
   */
  override def fairyRecoveryMana(uuid: UUID): F[FairyRecoveryMana] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val recoveryMana =
        sql"SELECT VotingFairyRecoveryValue FROM playerdata WHERE uuid = ${uuid.toString}"
          .map(_.int("VotingFairyRecoveryValue"))
          .single()
          .apply()
          .get
      FairyRecoveryMana(recoveryMana)
    }
  }
}
