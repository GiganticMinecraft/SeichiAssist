package com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain._
import com.github.unchama.seichiassist.subsystems.vote.subsystems.fairy.domain.property._
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcFairyPersistence[F[_]: Sync] extends FairyPersistence[F] {

  override def initializePlayerData(player: UUID): F[Unit] = Sync[F].delay {
    val playerDataCount = DB.readOnly { implicit session =>
      sql"SELECT COUNT(*) as c FROM vote_fairy where uuid = ${player.toString}"
        .map(_.int("c"))
        .single()
        .apply()
        .getOrElse(0)
    }

    if (playerDataCount == 0) {
      DB.localTx { implicit session =>
        sql"INSERT INTO vote_fairy (uuid) VALUES (${player.toString})".execute().apply()
      }
    }
  }

  override def updateAppleConsumeStrategy(
    player: UUID,
    openState: FairyAppleConsumeStrategy
  ): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET apple_open_state = ${openState.serializedValue} WHERE uuid = ${player.toString}"
          .execute()
          .apply()
      }
    }

  override def appleConsumeStrategy(player: UUID): F[FairyAppleConsumeStrategy] =
    Sync[F].delay {
      val serializedValue = DB.readOnly { implicit session =>
        sql"SELECT apple_open_state FROM vote_fairy WHERE uuid = ${player.toString}"
          .map(_.int("apple_open_state"))
          .single()
          .apply()
          .get
      }
      FairyAppleConsumeStrategy.values.find(_.serializedValue == serializedValue).get
    }

  override def updateFairySummonCost(player: UUID, fairySummonCost: FairySummonCost): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET fairy_summon_cost = ${fairySummonCost.value} WHERE uuid = ${player.toString}"
          .execute()
          .apply()
      }
    }

  override def fairySummonCost(player: UUID): F[FairySummonCost] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val fairySummonCost =
        sql"SELECT fairy_summon_cost FROM vote_fairy WHERE uuid = ${player.toString}"
          .map(_.int("fairy_summon_cost"))
          .single()
          .apply()
          .get
      FairySummonCost(fairySummonCost)
    }
  }

  override def updateIsFairyUsing(player: UUID, isFairyUsing: Boolean): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""UPDATE vote_fairy 
             | SET is_fairy_using = $isFairyUsing WHERE uuid = ${player.toString}"""
          .stripMargin
          .execute()
          .apply()
      }
    }

  override def isFairyUsing(player: UUID): F[Boolean] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT is_fairy_using FROM vote_fairy WHERE uuid = ${player.toString}"
        .map(_.boolean("is_fairy_using"))
        .single()
        .apply()
    }.get
  }

  override def updateFairyRecoveryMana(
    player: UUID,
    fairyRecoveryMana: FairyRecoveryMana
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"UPDATE vote_fairy SET fairy_recovery_mana_value = ${fairyRecoveryMana.recoveryMana} WHERE uuid = ${player.toString}"
        .execute()
        .apply()
    }
  }

  override def fairyRecoveryMana(player: UUID): F[FairyRecoveryMana] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val recoveryMana =
        sql"SELECT fairy_recovery_mana_value FROM vote_fairy WHERE uuid = ${player.toString}"
          .map(_.int("fairy_recovery_mana_value"))
          .single()
          .apply()
          .get
      FairyRecoveryMana(recoveryMana)
    }
  }

  override def updateFairyEndTime(player: UUID, fairyEndTime: FairyEndTime): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET fairy_end_time = ${fairyEndTime.endTime} WHERE uuid = ${player.toString}"
          .execute()
          .apply()
      }
    }

  override def fairyEndTime(player: UUID): F[Option[FairyEndTime]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val dateOpt = sql"SELECT fairy_end_time FROM vote_fairy WHERE uuid = ${player.toString}"
        .map(_.localDateTime("fairy_end_time"))
        .single()
        .apply()
      dateOpt.map(FairyEndTime)
    }
  }

  override def increaseConsumedAppleAmountByFairy(
    player: UUID,
    appleAmount: AppleAmount
  ): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET given_apple_amount = given_apple_amount + ${appleAmount.amount} WHERE uuid = ${player.toString}"
          .execute()
          .apply()
      }
    }

  override def consumedAppleAmountByFairy(player: UUID): F[Option[AppleAmount]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val appleAmountOpt =
          sql"SELECT given_apple_amount FROM vote_fairy WHERE uuid = ${player.toString}"
            .map(_.int("given_apple_amount"))
            .single()
            .apply()
        appleAmountOpt.map(AppleAmount)
      }
    }

  override def rankByConsumedAppleAmountByFairy(
    player: UUID
  ): F[Option[AppleConsumeAmountRank]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT vote_fairy.uuid AS uuid, name, given_apple_amount,
             | RANK() OVER(ORDER BY given_apple_amount DESC) AS rank
             | FROM vote_fairy 
             | INNER JOIN playerdata
             | ON (playerdata.uuid = vote_fairy.uuid)"""
          .stripMargin
          .map(rs =>
            rs.string("uuid") -> AppleConsumeAmountRank(
              rs.string("name"),
              rs.int("rank"),
              AppleAmount(rs.int("given_apple_amount"))
            )
          )
          .toList()
          .apply()
          .find(_._1 == player.toString)
          .map(_._2)
      }
    }

  override def fetchMostConsumedApplePlayersByFairy(
    top: Int
  ): F[Vector[AppleConsumeAmountRank]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"""SELECT name, given_apple_amount, RANK() OVER(ORDER BY given_apple_amount DESC) AS rank FROM vote_fairy
             | INNER JOIN playerdata ON (vote_fairy.uuid = playerdata.uuid) 
             | LIMIT $top;"""
          .stripMargin
          .map { rs =>
            val name = rs.string("name")
            val rank = rs.int("rank")
            val givenAppleAmount = rs.int("given_apple_amount")

            AppleConsumeAmountRank(name, rank, AppleAmount(givenAppleAmount))
          }
          .toList()
          .apply()
          .toVector
      }
    }

  override def totalConsumedAppleAmount: F[AppleAmount] = Sync[F].delay {
    DB.readOnly { implicit session =>
      val amount = sql"SELECT SUM(given_apple_amount) AS allAppleAmount FROM vote_fairy;"
        .map(_.int("allAppleAmount"))
        .single()
        .apply()
        .get
      AppleAmount(amount)
    }
  }

  override def setPlaySoundOnSpeech(player: UUID, playOnSpeech: Boolean): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"UPDATE vote_fairy SET is_play_fairy_speech_sound = $playOnSpeech WHERE uuid = ${player.toString}"
          .execute()
          .apply()
      }
    }

  override def playSoundOnFairySpeech(player: UUID): F[Boolean] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"SELECT is_play_fairy_speech_sound FROM vote_fairy WHERE uuid=${player.toString}"
          .map(_.boolean("is_play_fairy_speech_sound"))
          .single()
          .apply()
          .get
      }
    }
}
