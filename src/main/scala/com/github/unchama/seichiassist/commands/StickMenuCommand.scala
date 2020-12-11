package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.{layoutPreparationContext, syncShift}
import com.github.unchama.seichiassist.menus.stickmenu.StickMenu
import org.bukkit.command.TabExecutor

/*
* 棒メニューを開くコマンド
* @author KisaragiEffective
*/
object StickMenuCommand {
  val executor: TabExecutor = playerCommandBuilder
    .execution { context =>
      val sender = context.sender
      // TODO: 棒メニューを開くエフェクト
      IO(StickMenu.firstPage.open)
    }
    .build()
    .asNonBlockingTabExecutor()
}
