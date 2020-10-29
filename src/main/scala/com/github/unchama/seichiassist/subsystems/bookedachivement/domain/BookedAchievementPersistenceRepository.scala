package com.github.unchama.seichiassist.subsystems.bookedachivement.domain

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ResponseEffectOrResult
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

trait BookedAchievementPersistenceRepository[SyncContext[_], Key] {
  def bookAchievement(key: Key, achievementId: Int): SyncContext[Unit]

  def loadNotGivenBookedAchievementsOf(key: Key): SyncContext[List[Int]]

  def deleteBookedAchievementsOf(key: Key): SyncContext[Unit]

  def findPlayerUuid(playerName: String): SyncContext[Key]
}
