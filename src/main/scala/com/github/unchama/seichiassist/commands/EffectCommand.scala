package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.FastDiggingSettingsWriteApi
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.settings.FastDiggingEffectSuppressionState
import com.github.unchama.seichiassist.subsystems.fastdiggingeffect.domain.stats.FastDiggingEffectStatsSettings
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player

class EffectCommand[F[_]](api: FastDiggingSettingsWriteApi[IO, Player]) {
  private val printUsageExecutor = playerCommandBuilder.buildWithEffectAsExecution(
    MessageEffect(
      List(
        s"$YELLOW$BOLD[コマンドリファレンス]",
        s"$RED/ef",
        "採掘速度上昇効果の制限を変更することができます。",
        s"$RED/ef smart",
        "採掘速度上昇効果の内訳を表示するかしないかを変更することができます。"
      )
    )
  )

  import cats.implicits._

  private val toggleExecutor = playerCommandBuilder.buildWithEffectAsExecution {
    api.toggleEffectSuppression.flatMap { newState =>
      MessageEffect {
        newState match {
          case FastDiggingEffectSuppressionState.EnabledWithoutLimit =>
            s"${GREEN}採掘速度上昇効果:ON(無制限)"
          case limit: FastDiggingEffectSuppressionState.EnabledWithLimit =>
            s"${GREEN}採掘速度上昇効果:ON(${limit.limit}制限)"
          case FastDiggingEffectSuppressionState.Disabled =>
            s"${RED}採掘速度上昇効果:OFF"
        }
      }
    } >> MessageEffect("再度 /ef コマンドを実行することでトグルします。")
  }

  private val messageFlagToggleExecutor = playerCommandBuilder.buildWithEffectAsExecution {
    api.toggleStatsSettings.flatMap { newSettings =>
      MessageEffect {
        newSettings match {
          case FastDiggingEffectStatsSettings.AlwaysReceiveDetails =>
            s"${GREEN}内訳表示:ON(OFFに戻したい時は再度コマンドを実行します。)"
          case FastDiggingEffectStatsSettings.ReceiveTotalAmplifierOnUpdate =>
            s"${GREEN}内訳表示:OFF"
        }
      }
    }
  }

  val executor: TabExecutor = BranchedExecutor(
    Map("smart" -> messageFlagToggleExecutor),
    whenArgInsufficient = Some(toggleExecutor),
    whenBranchNotFound = Some(printUsageExecutor)
  ).asNonBlockingTabExecutor()
}
