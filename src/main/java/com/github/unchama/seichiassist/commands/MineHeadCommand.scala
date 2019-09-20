package com.github.unchama.seichiassist.commands

import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{StaticGachaPrizeFactory, Util}

object MineHeadCommand {
  val executor = playerCommandBuilder
      .execution { context =>
        Util.addItemToPlayerSafely(context.sender, StaticGachaPrizeFactory.getMineHeadItem())

        "${ChatColor.GREEN}専用アイテムを付与しました。".asMessageEffect()
      }
      .build()
      .asNonBlockingTabExecutor()
}
