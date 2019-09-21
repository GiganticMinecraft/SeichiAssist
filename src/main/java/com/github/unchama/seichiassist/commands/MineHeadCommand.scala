package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{StaticGachaPrizeFactory, Util}
import org.bukkit.ChatColor._

object MineHeadCommand {
  val executor = playerCommandBuilder
      .execution { context =>
        Util.addItemToPlayerSafely(context.sender, StaticGachaPrizeFactory.mineHeadItem())

        s"${GREEN}専用アイテムを付与しました。".asMessageEffect()
      }
      .build()
      .asNonBlockingTabExecutor()
}
