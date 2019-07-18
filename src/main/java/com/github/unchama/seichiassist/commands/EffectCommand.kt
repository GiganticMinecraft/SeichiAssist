package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.EmptyEffect
import com.github.unchama.targetedeffect.asMessageEffect
import com.github.unchama.targetedeffect.ops.plus

object EffectCommand {

  private val toggleExecutor = playerCommandBuilder
      .execution { context ->
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution EmptyEffect
        val toggleResponse = playerData.fastDiggingEffectSuppressor.suppressionDegreeToggleEffect
        val guidance = "再度 /ef コマンドを実行することでトグルします。".asMessageEffect()

        toggleResponse + guidance
      }
      .build()

  private val messageFlagToggleExecutor = playerCommandBuilder
      .execution { context ->
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution EmptyEffect

        playerData.toggleMessageFlag()
      }
      .build()

  val executor = BranchedExecutor(
      mapOf("smart" to messageFlagToggleExecutor),
      whenArgInsufficient = toggleExecutor
  ).asNonBlockingTabExecutor()

}
