package com.github.unchama.seichiassist.data.player.settings

import com.github.unchama.targetedeffect
import com.github.unchama.targetedeffect.TargetedEffect
import com.github.unchama.util.kotlin2scala.SuspendingMethod
import kotlin.Suppress
import org.bukkit.ChatColor._
import org.bukkit.command.CommandSender

class FastDiggingEffectSuppression {
  private var internalValue = 0

  val suppressionDegreeToggleEffect: TargetedEffect[CommandSender] =
      targetedeffect.UnfocusedEffect {
        internalValue = (internalValue + 1) % 6
      } + deferredEffect {
        when (internalValue) {
          0
          => s"${GREEN}採掘速度上昇効果:ON(無制限)"
          1
          => s"${GREEN}採掘速度上昇効果:ON(127制限)"
          2
          => s"${GREEN}採掘速度上昇効果:ON(200制限)"
          3
          => s"${GREEN}採掘速度上昇効果:ON(400制限)"
          4
          => s"${GREEN}採掘速度上昇効果:ON(600制限)"
          else => s"${RED}採掘速度上昇効果:OFF"
        }.asMessageEffect()
      }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def currentStatus(): String = {
    return s"${RESET}" + when(internalValue) {
      0
      => s"${GREEN}現在有効です(無制限)"
      1
      => s"${GREEN}現在有効です${YELLOW}(127制限)"
      2
      => s"${GREEN}現在有効です${YELLOW}(200制限)"
      3
      => s"${GREEN}現在有効です${YELLOW}(400制限)"
      4
      => s"${GREEN}現在有効です${YELLOW}(600制限)"
      else => s"${RED}現在OFFです"
    }
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def nextToggledStatus(): String = {
    return when (internalValue) {
      0 => "127制限"
      1 => "200制限"
      2 => "400制限"
      3 => "600制限"
      4 => "OFF"
      else => "無制限"
    }
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def isSuppressionActive(): Boolean = internalValue in 0..4

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def serialized(): Int = internalValue

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def setStateFromSerializedValue(value: Int) {
    internalValue = value
  }

  @Suppress("RedundantSuspendModifier")
  @SuspendingMethod def maximumAllowedEffectAmplifier(): Int = when (internalValue) {
    0 => 25565
    1 => 127
    2 => 200
    3 => 400
    4 => 600
    else => 0
  }
}