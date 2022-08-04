package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcFairyPersistence[F[_]: Sync] extends FairyPersistence[F] {

  /**
   * プレイヤーデータを作成する
   */
  def createPlayerData(uuid: UUID): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"INSERT IGNORE INTO vote_fairy (uuid) VALUES (${uuid.toString})".execute().apply()
    }
  }

  /**
   * 妖精に開放するりんごの状態を変更する
   */
  override def changeAppleOpenState(uuid: UUID, openState: FairyAppleConsumeStrategy): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET apple_open_state = ${openState.serializedValue} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精に開放するりんごの状態を取得する
   */
  override def appleOpenState(uuid: UUID): F[FairyAppleConsumeStrategy] =
    Sync[F].delay {
      val serializedValue = DB.readOnly { implicit session =>
        sql"SELECT apple_open_state FROM vote_fairy WHERE uuid = ${uuid.toString}"
          .map(_.int("apple_open_state"))
          .single()
          .apply()
          .get
      }
      FairyAppleConsumeStrategy.values.find(_.serializedValue == serializedValue).get
    }

  /**
   * 妖精を召喚するためのコストを更新する
   */
  override def updateFairySummonCost(uuid: UUID, fairySummonCost: FairySummonCost): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET fairy_summon_cost = ${fairySummonCost.value} WHERE uuid = ${uuid.toString}"
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
        sql"SELECT fairy_summon_cost FROM vote_fairy WHERE uuid = ${uuid.toString}"
          .map(_.int("fairy_summon_cost"))
          .single()
          .apply()
          .get
      FairySummonCost(fairySummonCost)
    }
  }

  /**
   * 妖精の召喚状態を更新します
   */
  override def updateIsFairyUsing(uuid: UUID, isFairyUsing: Boolean): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""UPDATE vote_fairy 
             | SET is_fairy_using = $isFairyUsing WHERE uuid = ${uuid.toString}"""
          .stripMargin
          .execute()
          .apply()
      }
    }

  /**
   * 妖精が召喚されているかを取得します
   */
  override def isFairyUsing(uuid: UUID): F[Boolean] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT is_fairy_using FROM vote_fairy WHERE uuid = ${uuid.toString}"
        .map(_.boolean("is_fairy_using"))
        .single()
        .apply()
    }.get
  }

  /**
   * 妖精が回復するマナの量を変更する
   */
  override def updateFairyRecoveryMana(
    uuid: UUID,
    fairyRecoveryMana: FairyRecoveryMana
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE vote_fairy SET fairy_recovery_mana_value = ${fairyRecoveryMana.recoveryMana} WHERE uuid = ${uuid.toString}"
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
        sql"SELECT fairy_recovery_mana_value FROM vote_fairy WHERE uuid = ${uuid.toString}"
          .map(_.int("fairy_recovery_mana_value"))
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
        sql"UPDATE vote_fairy SET fairy_end_time = ${fairyEndTime.endTimeOpt.get} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精の効果が終了する時刻を取得する
   */
  override def fairyEndTime(uuid: UUID): F[Option[FairyEndTime]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val dateOpt = sql"SELECT fairy_end_time FROM vote_fairy WHERE uuid = ${uuid.toString}"
        .map(_.localDateTime("fairy_end_time"))
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
        sql"UPDATE vote_fairy SET given_apple_amount = given_apple_amount + ${appleAmount.amount} WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精が食べたりんごの量を取得する
   */
  override def appleAteByFairy(uuid: UUID): F[Option[AppleAmount]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val appleAmountOpt =
        sql"SELECT given_apple_amount FROM vote_fairy WHERE uuid = ${uuid.toString}"
          .map(_.int("given_apple_amount"))
          .single()
          .apply()
      appleAmountOpt.map(AppleAmount)
    }
  }

  /**
   * 自分の妖精に食べさせたりんごの量の順位を返す
   */
  override def appleAteByFairyMyRanking(uuid: UUID): F[Option[AppleAteByFairyRank]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT vote_fairy.uuid AS uuid,name,given_apple_amount,COUNT(*) AS rank 
             | FROM vote_fairy 
             | INNER JOIN playerdata
             | ON (playerdata.uuid = vote_fairy.uuid)
             | ORDER BY rank DESC;"""
          .stripMargin
          .map(rs =>
            rs.string("uuid") -> AppleAteByFairyRank(
              rs.string("name"),
              rs.int("rank"),
              AppleAmount(rs.int("given_apple_amount"))
            )
          )
          .toList()
          .apply()
          .find(_._1 == uuid.toString)
          .map(_._2)
      }
    }

  /**
   * 妖精に食べさせたりんごの量の順位上位`number`件を返す
   */
  override def appleAteByFairyRanking(number: Int): F[Vector[Option[AppleAteByFairyRank]]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT name,given_apple_amount,COUNT(*) AS rank FROM vote_fairy 
             | INNER JOIN playerdata ON (vote_fairy.uuid = playerdata.uuid) 
             | ORDER BY rank DESC LIMIT $number;"""
          .stripMargin
          .map(rs => (rs.stringOpt("name"), rs.intOpt("rank"), rs.intOpt("given_apple_amount")))
          .toList()
          .apply()
          .map(data =>
            if (data._1.nonEmpty)
              Some(AppleAteByFairyRank(data._1.get, data._2.get, AppleAmount(data._3.get)))
            else None
          )
          .toVector
      }
    }

  /**
   * 妖精が食べたりんごの合計数を返す
   */
  override def allEatenAppleAmount: F[AppleAmount] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val amount = sql"SELECT SUM(given_apple_amount) AS allAppleAmount FROM vote_fairy;"
        .map(_.int("allAppleAmount"))
        .single()
        .apply()
        .get
      AppleAmount(amount)
    }
  }

  /**
   * 妖精が喋るときに音をだすかをトグルする
   */
  override def toggleFairySpeechSound(uuid: UUID, fairyPlaySound: Boolean): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET is_play_fairy_speech_sound = $fairyPlaySound WHERE uuid = ${uuid.toString}"
          .execute()
          .apply()
      }
    }

  /**
   * 妖精が喋ったときに音を再生するか取得する
   */
  override def fairySpeechSound(uuid: UUID): F[Boolean] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"SELECT is_play_fairy_speech_sound FROM vote_fairy WHERE uuid=${uuid.toString}"
          .map(_.boolean("is_play_fairy_speech_sound"))
          .single()
          .apply()
          .get
      }
    }
}
