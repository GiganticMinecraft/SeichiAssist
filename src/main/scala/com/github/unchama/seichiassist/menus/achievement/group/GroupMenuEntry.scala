package com.github.unchama.seichiassist.menus.achievement.group

import com.github.unchama.seichiassist.achievement.SeichiAchievement

sealed trait GroupMenuEntry

case class AchievementEntry(achievement: SeichiAchievement) extends GroupMenuEntry
object AchievementEntry {
  def within(range: Seq[Int]): List[AchievementEntry] =
    SeichiAchievement.values
      .toList
      .filter(achievement => range.contains(achievement.id))
      .map(AchievementEntry.apply)
}

case object Achievement8003UnlockEntry extends GroupMenuEntry
