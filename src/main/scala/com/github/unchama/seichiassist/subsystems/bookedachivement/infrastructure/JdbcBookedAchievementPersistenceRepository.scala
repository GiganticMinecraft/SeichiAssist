package com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure

import java.util.UUID

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.{AchievementOperation, BookedAchievementPersistenceRepository}
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcBookedAchievementPersistenceRepository[SyncContext[_]](implicit SyncContext: Sync[SyncContext])
  extends BookedAchievementPersistenceRepository[SyncContext, UUID] {

  /**
   * 指定した `achievementId` の実績付与・剥奪をプレイヤーの UUID である `key` とともに記録します.
   */
  override def bookAchievement(key: UUID, achievementId: Int, operation: AchievementOperation): SyncContext[Unit] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"""|insert into booked_achievement_status_change (player_uuid, achievement_id, operation)
              | values (${key.toString}, $achievementId, ${operation.toString})"""
          .stripMargin
          .update().apply()
      }
    }
  }

  /**
   * `key` を UUID に持つプレイヤーに適用されていない予約済み実績の番号を返します.
   */
  override def loadBookedAchievementsYetToBeAppliedOf(key: UUID): SyncContext[List[(AchievementOperation, Int)]] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"""|select achievement_id, operation from booked_achievement_status_change
              | where player_uuid = ${key.toString} and completed_at is null"""
          .stripMargin
          .map { rs =>
            (AchievementOperation.fromString(rs.string("operation")).get,
              rs.int("achievement_id"))
          }
          .toList().apply()
      }
    }
  }

  /**
   * `key` を `UUID` に持つプレイヤーの予約済み実績を受け取り済みとします.
   */
  override def setAllBookedAchievementsApplied(key: UUID): SyncContext[Unit] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"""|update booked_achievement_status_change set completed_at = cast(now() as datetime)
              | where player_uuid = ${key.toString} and completed_at is null"""
          .stripMargin
          .update().apply()
      }
    }
  }

  /**
   * プレイヤー名が `playerName` なプレイヤーの `UUID` を探します.
   */
  override def findPlayerUuid(playerName: String): SyncContext[UUID] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        UUID.fromString(
          sql"select (uuid) from playerdata where name = $playerName"
            .map { rs => rs.string("uuid")}
            .toList().apply().head
        )
      }
    }
  }
}
