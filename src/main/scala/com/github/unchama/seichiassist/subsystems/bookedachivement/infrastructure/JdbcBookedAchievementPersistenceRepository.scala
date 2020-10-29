package com.github.unchama.seichiassist.subsystems.bookedachivement.infrastructure

import java.util.UUID

import cats.effect.Sync
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.BookedAchievementPersistenceRepository
import scalikejdbc.{DB, scalikejdbcSQLInterpolationImplicitDef}

class JdbcBookedAchievementPersistenceRepository[SyncContext[_] : Sync](implicit SyncContext: Sync[SyncContext])
  extends BookedAchievementPersistenceRepository[SyncContext, UUID] {

  /**
   * 指定した `achievementId` の実績をプレイヤーの UUID である `key` とともに記録します.
   */
  override def bookAchievement(key: UUID, achievementId: Int): SyncContext[Unit] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"insert into booked_achievement (player_uuid, achievement_id) values (${key.toString}, $achievementId)"
          .update().apply()
      }
    }
  }

  /**
   * `key` を UUID に持つプレイヤーがまだ受け取っていない予約済み実績の番号を返します.
   */
  override def loadNotGivenBookedAchievementsOf(key: UUID): SyncContext[List[Int]] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"select achievement_id from booked_achievement where player_uuid = ${key.toString}"
          .map { rs => rs.int("achievement_id") }
          .toList().apply()
      }
    }
  }

  /**
   * `key` を UUID に持つプレイヤーの未受け取り予約済みの実績を全て削除(=受け取り済みに)します.
   */
  override def deleteBookedAchievementsOf(key: UUID): SyncContext[Unit] = {
    SyncContext.delay {
      DB.localTx { implicit session =>
        sql"delete from booked_achievement where player_uuid = ${key.toString}"
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
