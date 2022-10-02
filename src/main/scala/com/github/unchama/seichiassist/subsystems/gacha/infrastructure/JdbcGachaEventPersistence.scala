package com.github.unchama.seichiassist.subsystems.gacha.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaEvent,
  GachaEventPersistence
}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcGachaEventPersistence[F[_]: Sync] extends GachaEventPersistence[F] {

  override def registerGachaEvent(gachaEvent: GachaEvent): F[Unit] =
    Sync[F].delay {
      DB.localTx { implicit session =>
        sql"""INSERT INTO gacha_events 
             | (event_name, event_start_time, event_end_time) VALUES 
             | (${gachaEvent.eventName}, ${gachaEvent.getStartTimeString}, ${gachaEvent.getEndTimeString})
           """.stripMargin.execute().apply()
      }
    }

  override def deleteGachaEvent(gachaEvent: GachaEvent): F[Unit] = ???

  override def gachaEvents: F[Vector[GachaEvent]] = ???
}
