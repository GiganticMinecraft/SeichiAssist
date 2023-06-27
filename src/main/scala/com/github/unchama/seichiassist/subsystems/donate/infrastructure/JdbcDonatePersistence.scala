package com.github.unchama.seichiassist.subsystems.donate.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.seichiskill.effect.ActiveSkillPremiumEffect
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePersistence,
  DonatePremiumEffectPoint,
  Obtained,
  PlayerName,
  PremiumEffectPurchaseData,
  Used
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

import java.util.UUID

class JdbcDonatePersistence[F[_]: Sync] extends DonatePersistence[F] {

  override def addDonatePremiumEffectPoint(
    playerName: PlayerName,
    obtainedPremiumEffectPoint: Obtained
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""INSERT INTO donate_purchase_history 
           | (uuid, get_points, timestamp)
           | VALUES 
           | ((SELECT uuid FROM playerdata WHERE name = ${playerName.name}),
           | ${obtainedPremiumEffectPoint.effectPoint.value},
           | ${obtainedPremiumEffectPoint.purchaseDate})""".stripMargin.execute().apply()
    }
  }

  override def useDonatePremiumEffectPoint(
    uuid: UUID,
    effect: ActiveSkillPremiumEffect
  ): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"INSERT INTO donate_usage_history (uuid, effect_name, use_points) VALUES (${uuid.toString}, ${effect.entryName}, ${effect.usePoint})"
          .execute()
          .apply()
      }
    }

  override def currentPremiumEffectPoints(uuid: UUID): F[DonatePremiumEffectPoint] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        val premiumEffectPointsOpt =
          sql"""SELECT (
               | SELECT COALESCE(SUM(get_points), 0) AS sum_get_points FROM donate_purchase_history
               | WHERE uuid = ${uuid.toString}) - (
               | SELECT COALESCE(SUM(use_points), 0) AS sum_use_points FROM donate_usage_history 
               | WHERE uuid = ${uuid.toString}) AS currentPremiumEffectPoints
             """.stripMargin.map(_.int("currentPremiumEffectPoints")).single().apply()
        DonatePremiumEffectPoint(premiumEffectPointsOpt.get)
      }
    }

  override def donatePremiumEffectPointPurchaseHistory(
    uuid: UUID
  ): F[Vector[PremiumEffectPurchaseData]] =
    Sync[F].delay {
      DB.readOnly { implicit session =>
        sql"SELECT get_points,timestamp FROM donate_purchase_history WHERE uuid = ${uuid.toString}"
          .map(rs =>
            Obtained(DonatePremiumEffectPoint(rs.int("get_points")), rs.localDate("timestamp"))
          )
          .list()
          .apply()
          .toVector
      }
    }

  override def donatePremiumEffectPointUsageHistory(
    uuid: UUID
  ): F[Vector[PremiumEffectPurchaseData]] = Sync[F].delay {
    DB.readOnly { implicit session =>
      sql"SELECT effect_name, use_points, timestamp FROM donate_usage_history WHERE uuid = ${uuid.toString}"
        .map(rs =>
          Used(
            DonatePremiumEffectPoint(rs.int("use_points")),
            rs.localDate("timestamp"),
            ActiveSkillPremiumEffect.withName(rs.string("effect_name"))
          )
        )
        .list()
        .apply()
        .toVector
    }
  }

}
