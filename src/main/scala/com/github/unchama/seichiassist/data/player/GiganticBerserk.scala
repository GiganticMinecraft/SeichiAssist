package com.github.unchama.seichiassist.data.player

import com.github.unchama.seichiassist.LevelThresholds
import org.bukkit.Material

case class GiganticBerserk(level: Int = 0, exp: Int = 0, stage: Int = 0, canEvolve: Boolean = false, cd: Int = 0) {

  def reachedLimit(): Boolean = stage == 4 && level == 9

  def manaRegenerationProbability(): Double =
    if (level < 2) 0.05
    else if (level < 4) 0.06
    else if (level < 6) 0.07
    else if (level < 8) 0.08
    else if (level < 9) 0.09
    else 0.10

  def materialOnUI(): Material = {
    stage match {
      case 0 => Material.WOOD_SWORD
      case 1 => Material.STONE_SWORD
      case 2 => Material.GOLD_SWORD
      case 3 => Material.IRON_SWORD
      case 4 => Material.DIAMOND_SWORD
      case _ => throw new RuntimeException("This branch should not be reached")
    }
  }

  /**
   * 現在の `level` と `stage` において、次の `level` までに倒す必要がある敵の数(= `exp`)を返します.
   * @return 次の `level` までに倒す必要がある敵の数(= `exp`)
   */
  def requiredExpToNextLevel(): Int = {
    val current = stage * 10 + level
    LevelThresholds.giganticBerserkLevelList(current)
  }
}
