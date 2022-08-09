package com.github.unchama.seichiassist.subsystems.donate.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.donate.domain.{
  DonatePersistence,
  DonatePremiumEffectPoint,
  PlayerName
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcDonatePersistence[F[_]: Sync] extends DonatePersistence[F] {

  override def addDonatePremiumEffectPoint(
    playerName: PlayerName,
    donatePremiumEffectPoint: DonatePremiumEffectPoint
  ): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"""INSERT INTO donate_purchase_history 
           | (uuid, get_points) 
           | VALUES 
           | (SELECT uuid FROM playerdata WHERE name = ${playerName.name}, ${donatePremiumEffectPoint.value})"""
        .stripMargin
        .execute()
        .apply()
    }
  }

}
