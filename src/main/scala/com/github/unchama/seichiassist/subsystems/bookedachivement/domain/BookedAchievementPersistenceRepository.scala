package com.github.unchama.seichiassist.subsystems.bookedachivement.domain

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ResponseEffectOrResult
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

trait BookedAchievementPersistenceRepository[SyncContext[_], Key] {
  def bookAchievement(key: Key, achievementId: Int, operation: AchievementOperation): SyncContext[Unit]

  def loadNotAppliedBookedAchievementsOf(key: Key): SyncContext[List[(AchievementOperation, Int)]]

  def setAllBookedAchievementsApplied(key: Key): SyncContext[Unit]

  def findPlayerUuid(playerName: String): SyncContext[Key]
}
