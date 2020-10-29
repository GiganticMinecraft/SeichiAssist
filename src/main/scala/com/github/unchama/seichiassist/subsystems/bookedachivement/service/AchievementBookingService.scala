package com.github.unchama.seichiassist.subsystems.bookedachivement.service

import java.util.UUID

import cats.data.EitherT
import cats.effect.SyncEffect
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.BookedAchievementPersistenceRepository

class AchievementBookingService[
  SyncContext[_] : SyncEffect
](implicit persistenceRepository: BookedAchievementPersistenceRepository[SyncContext, UUID]
 ) {

  import cats.implicits._

  def loadBookedAchievementsIds(uuid: UUID): SyncContext[Either[String, List[Int]]] = {
    {
      for {
        result <- EitherT(loadNotGivenBookedAchivementsOrError(uuid))
        _ <- EitherT(deleteAllBookedAchievementsOrError(uuid))
      } yield result
    }.value
  }

  def writeAchivementId(playerName: String, achievementId: Int): SyncContext[Either[String, Unit]] = {
    {
      for {
        uuid <- EitherT(findUUIDByPlayerNameOrError(playerName))
        _ <- EitherT(bookAchievement(uuid, achievementId))
      } yield ()
    }.value
  }

  private def bookAchievement(uuid: UUID, achievementId: Int): SyncContext[Either[String, Unit]] = {
    for {
      result <- persistenceRepository.bookAchievement(uuid, achievementId).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] 実績 (No. $achievementId) をプレイヤー (UUID = $uuid) に正常に予約できませんでした。"
    }
  }

  private def findUUIDByPlayerNameOrError(playerName: String): SyncContext[Either[String, UUID]] = {
    for {
      result <- persistenceRepository.findPlayerUuid(playerName).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] プレイヤー ($playerName) のUUIDを発見できませんでした。"
    }
  }

  private def loadNotGivenBookedAchivementsOrError(uuid: UUID): SyncContext[Either[String, List[Int]]] = {
    for {
      result <- persistenceRepository.loadNotGivenBookedAchievementsOf(uuid).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] プレイヤー (UUID = $uuid) に実績を正常に与えられませんでした。"
    }
  }

  private def deleteAllBookedAchievementsOrError(uuid: UUID): SyncContext[Either[String, Unit]] = {
    for {
      result <- persistenceRepository.deleteBookedAchievementsOf(uuid).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] プレイヤー (UUID = $uuid) の予約済み実績を正常に削除できませんでした。"
    }
  }
}
