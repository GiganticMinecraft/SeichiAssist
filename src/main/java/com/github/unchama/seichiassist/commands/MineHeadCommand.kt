package com.github.unchama.seichiassist.commands

import com.github.unchama.contextualexecutor.asNonBlockingTabExecutor
import com.github.unchama.contextualexecutor.builder.response.asResponseToSender
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.Util
import org.bukkit.ChatColor

object MineHeadCommand {
  val executor = playerCommandBuilder
      .execution { context ->
        Util.addItemToPlayerSafely(context.sender, Util.getMineHeadItem())

        "${ChatColor.GREEN}専用アイテムを付与しました。".asResponseToSender()
      }
      .build()
      .asNonBlockingTabExecutor()
}
