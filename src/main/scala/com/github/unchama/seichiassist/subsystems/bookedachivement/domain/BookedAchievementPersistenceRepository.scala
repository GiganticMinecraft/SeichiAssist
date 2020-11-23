package com.github.unchama.seichiassist.subsystems.bookedachivement.domain

trait BookedAchievementPersistenceRepository[AsyncContext[_], Key] {
  def bookAchievement(key: Key, achievementId: Int, operation: AchievementOperation): AsyncContext[Unit]

  def loadBookedAchievementsYetToBeAppliedOf(key: Key): AsyncContext[List[(AchievementOperation, Int)]]

  def setAllBookedAchievementsApplied(key: Key): AsyncContext[Unit]

  def findPlayerUuid(playerName: String): AsyncContext[Key]
}
