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
    donatePremiumEffectPoint: DonatePremiumEffectPoint
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""INSERT INTO donate_purchase_history 
           | (uuid, get_points) 
           | VALUES 
           | ((SELECT uuid FROM playerdata WHERE name = ${playerName.name};), ${donatePremiumEffectPoint.value})"""
        .stripMargin
        .execute()
        .apply()
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
          sql"""SELECT
               |   COALESCE(SUM(purchase_history.get_points) - SUM(usage_history.use_points), 0) AS currentPremiumEffectPoints
               | FROM
               |   donate_usage_history usage_history
               | LEFT JOIN donate_purchase_history purchase_history ON
               |   usage_history.uuid = purchase_history.uuid
               | WHERE usage_history.uuid = ${uuid.toString}"""
            .stripMargin
            .map(_.int("currentPremiumEffectPoints"))
            .single()
            .apply()
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
          .toList()
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
        .toList()
        .apply()
        .toVector
    }
  }

}
