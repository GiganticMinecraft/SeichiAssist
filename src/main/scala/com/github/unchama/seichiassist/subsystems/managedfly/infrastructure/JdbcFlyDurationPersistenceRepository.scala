package com.github.unchama.seichiassist.subsystems.managedfly.infrastructure

import java.util.UUID

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.managedfly.application.FlyDurationPersistenceRepository
import com.github.unchama.seichiassist.subsystems.managedfly.domain.RemainingFlyDuration
import scalikejdbc._

class JdbcFlyDurationPersistenceRepository[SyncContext[_]](implicit SyncContext: Sync[SyncContext])
  extends FlyDurationPersistenceRepository[SyncContext, UUID] {

  override def writePair(key: UUID, duration: Option[RemainingFlyDuration]): SyncContext[Unit] = SyncContext.delay {
    DB.localTx { implicit session =>
      val serializedDuration = duration match {
        case Some(RemainingFlyDuration.PositiveMinutes(n)) => n
        case Some(RemainingFlyDuration.Infinity) => -1
        case None => 0
      }

      sql"""
        insert into fly_status_cache values (${key.toString}, $serializedDuration)
          on duplicate key update remaining_fly_minutes = $serializedDuration
      """.stripMargin
        .update().apply()
    }
  }

  override def read(key: UUID): SyncContext[Option[RemainingFlyDuration]] = SyncContext.delay {
    DB.localTx { implicit session =>
      sql"""
        select remaining_fly_minutes from fly_status_cache
          where player_uuid = ${key.toString}
      """
        .map { rs => rs.int("remaining_fly_minutes") }
        .headOption().apply()
        .flatMap {
          case 0 => None
          case -1 => Some(RemainingFlyDuration.Infinity)
          case n if n > 0 => Some(RemainingFlyDuration.PositiveMinutes.fromPositive(n))
          case _ => throw new IllegalStateException("DB contains out-of-range value")
        }
    }
  }
}
