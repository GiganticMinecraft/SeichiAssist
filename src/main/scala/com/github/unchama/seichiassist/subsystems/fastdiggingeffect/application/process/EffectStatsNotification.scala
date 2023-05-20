package com.github.unchama.seichiassist.subsystems.fastdiggingeffect.application.process

import cats.effect.ConcurrentEffect
import com.github.unchama.minecraft.actions.SendMinecraftMessage
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.{
  EffectListDiff,
  FastDiggingEffectStatsSettings
}
import org.bukkit.ChatColor.{RED, RESET, WHITE, YELLOW}

object EffectStatsNotification {

  def using[F[_]: ConcurrentEffect: SendMinecraftMessage[*[_], Player], Player](
    effectDiffWithSettings: fs2.Stream[
      F,
      (Player, (EffectListDiff, FastDiggingEffectStatsSettings))
    ]
  ): fs2.Stream[F, Unit] =
    effectDiffWithSettings.evalMap {
      case (player, (effectListDiff, settings)) =>
        val shouldNotifyNewValue = settings match {
          case FastDiggingEffectStatsSettings.AlwaysReceiveDetails => true
          case FastDiggingEffectStatsSettings.ReceiveTotalAmplifierOnUpdate =>
            effectListDiff.hasDifference
        }

        val newEffectList = effectListDiff.newList

        val shouldNotifyEffectStats =
          (settings == FastDiggingEffectStatsSettings.AlwaysReceiveDetails) &&
            newEffectList.nonEmpty

        val messages = {
          val ofNewValue = {
            val normalizedLevel = effectListDiff.newEffectAmplifier.normalizedEffectLevel
            s"$YELLOW★${WHITE}採掘速度上昇レベルが$YELLOW$normalizedLevel${WHITE}になりました"
          }

          val ofDetails = List(
            "----------------------------内訳-----------------------------"
          ) ++ newEffectList.map { effect =>
            s"$RESET$RED+${effect.amplifier.formatted} ${effect.cause.description}"
          } ++ List("-------------------------------------------------------------")

          Option.when(shouldNotifyNewValue)(ofNewValue).toList ++
            Option.when(shouldNotifyEffectStats)(ofDetails).toList.flatten
        }

        SendMinecraftMessage[F, Player].list(player, messages)
    }
}
