package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
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
   * 妖精が有効な時間の状態を更新する
   */
  override def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET toggleVotingFairy = ${fairySummonCost.value} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精を召喚するためのコストを取得する
   */
  override def fairySummonCost(uuid: UUID): F[FairySummonCost] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val fairySummonCost =
        sql"SELECT toggleVotingFairy FROM playerdata WHERE uuid = ${uuid.toString}"
          .map(_.int("toggleVotingFairy"))
          .single()
          .apply()
          .get
      FairySummonCost(fairySummonCost)
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
      sql"UPDATE playerdata SET VotingFairyRecoveryValue = ${fairyRecoveryMana.recoveryMana} WHERE uuid = ${uuid.toString}"
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

  /**
   * 妖精の効果が終了する時刻を変更する
   */
  override def updateFairyEndTime(uuid: UUID, fairyEndTime: FairyEndTime): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET newVotingFairyTime = ${fairyEndTime.endTimeOpt.get} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精の効果が終了する時刻を取得する
   */
  override def fairyEndTime(uuid: UUID): F[Option[FairyEndTime]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val dateOpt = sql"SELECT newVotingFairyTime FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.localDateTime("newVotingFairyTime"))
        .single()
        .apply()
      dateOpt.map { date => FairyEndTime(Some(date)) }
    }
  }

  /**
   * 妖精が食べたりんごの量を増加させる
   */
  override def increaseAppleAteByFairy(uuid: UUID, appleAmount: AppleAmount): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE playerdata SET p_apple = p_apple + ${appleAmount.amount} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精が食べたりんごの量を取得する
   */
  override def appleAteByFairy(uuid: UUID): F[AppleAmount] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val appleAmountOpt = sql"SELECT p_apple FROM playerdata WHERE uuid = ${uuid.toString}"
        .map(_.int("p_apple"))
        .single()
        .apply()
      AppleAmount(appleAmountOpt.get)
    }
  }

  /**
   * 自分の妖精に食べさせたりんごの量の順位を返す
   */
  override def appleAteByFairyMyRanking(uuid: UUID): F[AppleAteByFairyRank] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val rank = sql"SELECT name,p_apple,COUNT(*) AS rank FROM playerdata ORDER BY DESC;"
        .map(_.int("rank"))
        .single()
        .apply()
      AppleAteByFairyRank(rank.get)
    }
  }

  /**
   * 妖精に食べさせたりんごの量の順位上位4件を返す
   */
  override def appleAteByFairyRankingTopFour(uuid: UUID): F[AppleAteByFairyRankTopFour] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val topFour =
          sql"SELECT name,p_apple,COUNT(*) AS rank FROM playerdata ORDER BY DESC LIMIT 4;"
            .map(_.intOpt("rank"))
            .toList()
            .apply()
            .map(_.map(AppleAteByFairyRank))

        AppleAteByFairyRankTopFour(topFour.head.get, topFour(1), topFour(2), topFour(3))
      }
    }
}
