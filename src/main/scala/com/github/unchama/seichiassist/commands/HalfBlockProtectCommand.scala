package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.SeichiAssist
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import org.bukkit.command.TabExecutor

object HalfBlockProtectCommand {
  val executor: TabExecutor = playerCommandBuilder
    .buildWithExecutionCSEffect { context =>
      SeichiAssist.playermap(context.sender.getUniqueId).settings.toggleHalfBreakFlag
    }
    .build()
    .asNonBlockingTabExecutor()
}
