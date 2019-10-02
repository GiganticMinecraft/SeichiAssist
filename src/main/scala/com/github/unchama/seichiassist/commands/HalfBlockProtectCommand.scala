package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import org.bukkit.command.TabExecutor

object HalfBlockProtectCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val playerData = SeichiAssist.playermap(context.sender.getUniqueId)
      IO(playerData.settings.toggleHalfBreakFlag)
    }
    .build()
    .asNonBlockingTabExecutor()
}