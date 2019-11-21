package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.util.{StaticGachaPrizeFactory, Util}
import com.github.unchama.targetedeffect.syntax._
import org.bukkit.ChatColor._
import org.bukkit.command.TabExecutor

object MineHeadCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      Util.addItemToPlayerSafely(context.sender, StaticGachaPrizeFactory.getMineHeadItem)
      IO(s"${GREEN}専用アイテムを付与しました。".asMessageEffect())
    }
    .build()
    .asNonBlockingTabExecutor()
}
