package com.github.unchama.seichiassist.subsystems.bookedachivement.domain

trait BookedAchievementPersistenceRepository[F[_], Key] {
  def bookAchievement(key: Key, achievementId: Int, operation: AchievementOperation): F[Unit]

  def loadBookedAchievementsYetToBeAppliedOf(key: Key): F[List[(AchievementOperation, Int)]]

  def setAllBookedAchievementsApplied(key: Key): F[Unit]

  def findPlayerUuid(playerName: String): F[Key]
}
