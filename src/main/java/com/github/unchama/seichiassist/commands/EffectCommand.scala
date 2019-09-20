package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder

object EffectCommand {
  private val printUsageExecutor: ContextualExecutor = playerCommandBuilder
      .execution {
        List(
            s"${ChatColor.YELLOW}${ChatColor.BOLD}[コマンドリファレンス]",
            s"${ChatColor.RED}/ef",
            "採掘速度上昇効果の制限を変更することができます。",
            s"${ChatColor.RED}/ef smart",
            "採掘速度上昇効果の内訳を表示するかしないかを変更することができます。"
        ).asMessageEffect()
      }
      .build()

  private val toggleExecutor = playerCommandBuilder
      .execution { context =>
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution EmptyEffect
        val toggleResponse = playerData.settings.fastDiggingEffectSuppression.suppressionDegreeToggleEffect
        val guidance = "再度 /ef コマンドを実行することでトグルします。".asMessageEffect()

        toggleResponse + guidance
      }
      .build()

  private val messageFlagToggleExecutor = playerCommandBuilder
      .execution { context =>
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution EmptyEffect

        playerData.toggleMessageFlag()
      }
      .build()

  val executor = BranchedExecutor(
      mapOf("smart" to messageFlagToggleExecutor),
      whenArgInsufficient = toggleExecutor, whenBranchNotFound = printUsageExecutor
  ).asNonBlockingTabExecutor()

}
