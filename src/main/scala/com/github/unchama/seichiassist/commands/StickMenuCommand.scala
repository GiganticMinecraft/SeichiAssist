package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, StickMenu}
import org.bukkit.command.TabExecutor

/*
* 棒メニューを開くコマンド
* @author KisaragiEffective
*/
object StickMenuCommand {
  def executor(implicit ioCanOpenStickMenuFirstPage: IO CanOpen FirstPage.type): TabExecutor = playerCommandBuilder
    .execution { _ => IO.pure(ioCanOpenStickMenuFirstPage.open(StickMenu.firstPage)) }
    .build()
    .asNonBlockingTabExecutor()
}
