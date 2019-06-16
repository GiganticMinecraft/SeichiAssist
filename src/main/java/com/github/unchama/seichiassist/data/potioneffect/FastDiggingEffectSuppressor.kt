package com.github.unchama.seichiassist.data.potioneffect

import com.github.unchama.effect.MessageToSender
import com.github.unchama.effect.asResponseToSender
import org.bukkit.ChatColor

class FastDiggingEffectSuppressor {
  var internalValue = 0

  @Suppress("RedundantSuspendModifier")
  suspend fun toggleEffect(): MessageToSender {
    internalValue = (internalValue + 1) % 6

    val responseMessage = when (internalValue) {
      0 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(無制限)"
      1 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(127制限)"
      2 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(200制限)"
      3 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(400制限)"
      4 -> "${ChatColor.GREEN}採掘速度上昇効果:ON(600制限)"
      else -> "${ChatColor.RED}採掘速度上昇効果:OFF"
    }

    return responseMessage.asResponseToSender()
  }

  fun currentStatus(): String {
    return "${ChatColor.RESET}" + when (internalValue) {
      0 -> "${ChatColor.GREEN}現在有効です(無制限)"
      1 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(127制限)"
      2 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(200制限)"
      3 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(400制限)"
      4 -> "${ChatColor.GREEN}現在有効です${ChatColor.YELLOW}(600制限)"
      else -> "${ChatColor.RED}現在OFFです"
    }
  }

  fun nextStatus(): String {
    return when (internalValue) {
      0 -> "127制限"
      1 -> "200制限"
      2 -> "400制限"
      3 -> "600制限"
      4 -> "OFF"
      else -> "無制限"
    }
  }

  fun isSuppressionActive(): Boolean = internalValue in 0..4

  fun serializedSuppressionState(): Int = internalValue

  fun setStateFromSerializedValue(value: Int) {
    internalValue = value
  }

  fun maximumAllowedEffectAmplifier() = when (internalValue) {
    0 -> 25565
    1 -> 127
    2 -> 200
    3 -> 400
    4 -> 600
    else -> 0
  }
}