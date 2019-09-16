package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.targetedeffect.EmptyEffect

object HalfBlockProtectCommand {
  val executor = playerCommandBuilder
      .execution { context ->
        val playerData = SeichiAssist.playermap[context.sender.uniqueId] ?: return@execution EmptyEffect
        playerData.settings.toggleHalfBreakFlag()
      }
      .build()
      .asNonBlockingTabExecutor()
}