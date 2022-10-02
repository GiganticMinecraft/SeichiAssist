package com.github.unchama.seichiassist.subsystems.gacha.infrastructure

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.gacha.domain.{
  GachaEvent,
  GachaEventName,
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

  override def deleteGachaEvent(eventName: GachaEventName): F[Unit] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"DELETE FROM gacha_events WHERE event_name = ${eventName.name}".execute().apply()
    }
  }

  override def gachaEvents: F[Vector[GachaEvent]] = Sync[F].delay {
    DB.localTx { implicit session =>
      sql"SELECT event_name, event_start_time, event_end_time FROM gacha_events"
        .map { rs =>
          GachaEvent(
            GachaEventName(rs.string("event_name")),
            rs.localDateTime("event_start_time"),
            rs.localDateTime("event_end_time")
          )
        }
        .toList()
        .apply()
        .toVector
    }
  }
}
