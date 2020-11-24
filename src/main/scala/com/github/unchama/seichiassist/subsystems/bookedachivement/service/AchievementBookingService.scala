package com.github.unchama.seichiassist.subsystems.bookedachivement.service

import java.util.UUID

import cats.FlatMap
import com.github.unchama.seichiassist.subsystems.bookedachivement.domain.{AchievementOperation, BookedAchievementPersistenceRepository}

class AchievementBookingService[
  F[_] : FlatMap
](implicit persistenceRepository: BookedAchievementPersistenceRepository[F, UUID]) {

  import cats.implicits._
  import persistenceRepository._

  def loadBookedAchievementsIds(uuid: UUID): F[List[(AchievementOperation, Int)]] = {
    for {
      result <- loadBookedAchievementsYetToBeAppliedOf(uuid)
      _ <- setAllBookedAchievementsApplied(uuid)
    } yield result
  }

  def writeAchivementId(playerName: String, achievementId: Int, operation: AchievementOperation): F[Unit] = {
    for {
      uuid <- findPlayerUuid(playerName)
      _ <- bookAchievement(uuid, achievementId, operation)
    } yield ()
  }
}
