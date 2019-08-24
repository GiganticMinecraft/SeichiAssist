package com.github.unchama.seichiassist.data.player.settings

import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.targetedeffect.asMessageEffect
import com.github.unchama.targetedeffect.deferredEffect
import com.github.unchama.targetedeffect.ops.plus
import com.github.unchama.targetedeffect.unfocusedEffect
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender

class FastDiggingEffectSuppression {
  private var internalValue = 0

  val suppressionDegreeToggleEffect: TargetedEffect<CommandSender> =
      unfocusedEffect {
        internalValue = (internalValue + 1) % 6
      } + deferredEffect {
        when (internalValue) {
          0 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(無制限)"
          1 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(127制限)"
          2 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(200制限)"
          3 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(400制限)"
          4 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(600制限)"
          else -> "${ChatColor.RED}採掘速度上昇効果:OFF"
        }.asMessageEffect()
      }

  @Suppress("RedundantSuspendModifier")
  suspend fun currentStatus(): String {
    return "${ChatColor.RESET}" + when (internalValue) {
      0 -> "${ChatColor.GREEN}現在有効です(無制限)"
      1 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(127制限)"
      2 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(200制限)"
      3 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(400制限)"
      4 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(600制限)"
      else -> "${ChatColor.RED}現在OFFです"
    }
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun nextToggledStatus(): String {
    return when (internalValue) {
      0 -> "127制限"
      1 -> "200制限"
      2 -> "400制限"
      3 -> "600制限"
      4 -> "OFF"
      else -> "無制限"
    }
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun isSuppressionActive(): Boolean = internalValue in 0..4

  @Suppress("RedundantSuspendModifier")
  suspend fun serialized(): Int = internalValue

  @Suppress("RedundantSuspendModifier")
  suspend fun setStateFromSerializedValue(value: Int) {
    internalValue = value
  }

  @Suppress("RedundantSuspendModifier")
  suspend fun maximumAllowedEffectAmplifier(): Int = when (internalValue) {
    0 -> 25565
    1 -> 127
    2 -> 200
    3 -> 400
    4 -> 600
    else -> 0
  }
}