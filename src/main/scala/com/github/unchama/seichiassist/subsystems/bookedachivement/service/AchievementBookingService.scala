package com.github.unchama.seichiassist.subsystems.bookedachivement.service

import java.util.UUID

import cats.data.EitherT
import cats.effect.ConcurrentEffect
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.{AchievementOperation, BookedAchievementPersistenceRepository}

class AchievementBookingService[
  AsyncContext[_] : ConcurrentEffect
](implicit persistenceRepository: BookedAchievementPersistenceRepository[AsyncContext, UUID]
 ) {

  import cats.implicits._

  def loadBookedAchievementsIds(uuid: UUID): AsyncContext[Either[String, List[(AchievementOperation, Int)]]] = {
    {
      for {
        result <- EitherT(loadBookedAchivementsYetToBeAppliedOrError(uuid))
        _ <- EitherT(setAllBookedAchievementsAppliedOrError(uuid))
      } yield result
    }.value
  }

  def writeAchivementId(playerName: String, achievementId: Int, operation: AchievementOperation): AsyncContext[Either[String, Unit]] = {
    {
      for {
        uuid <- EitherT(findUUIDByPlayerNameOrError(playerName))
        _ <- EitherT(bookAchievement(uuid, achievementId, operation))
      } yield ()
    }.value
  }

  private def bookAchievement(uuid: UUID, achievementId: Int, operation: AchievementOperation): AsyncContext[Either[String, Unit]] = {
    for {
      result <- persistenceRepository.bookAchievement(uuid, achievementId, operation).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] 実績 (No. $achievementId) をプレイヤー (UUID = $uuid) に正常に予約できませんでした。"
    }
  }

  private def findUUIDByPlayerNameOrError(playerName: String): AsyncContext[Either[String, UUID]] = {
    for {
      result <- persistenceRepository.findPlayerUuid(playerName).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] プレイヤー ($playerName) のUUIDを発見できませんでした。"
    }
  }

  private def loadBookedAchivementsYetToBeAppliedOrError(uuid: UUID): AsyncContext[Either[String, List[(AchievementOperation, Int)]]] = {
    for {
      result <- persistenceRepository.loadBookedAchievementsYetToBeAppliedOf(uuid).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] プレイヤー (UUID = $uuid) に実績を正常に与えられませんでした。"
    }
  }

  private def setAllBookedAchievementsAppliedOrError(uuid: UUID): AsyncContext[Either[String, Unit]] = {
    for {
      result <- persistenceRepository.setAllBookedAchievementsApplied(uuid).attempt
    } yield result.leftMap { _ =>
      s"[実績予約システム] プレイヤー (UUID = $uuid) の予約済み実績を正常に削除できませんでした。"
    }
  }
}
