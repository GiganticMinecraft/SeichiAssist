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
  val effect = StickMenu.firstPage.open
  val executor: TabExecutor = playerCommandBuilder
    .execution { _ => IO.pure(effect) }
    .build()
    .asNonBlockingTabExecutor()
}
