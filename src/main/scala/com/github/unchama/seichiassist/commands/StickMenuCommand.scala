package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.ContextualExecutor
import com.github.unchama.contextualexecutor.executors.BranchedExecutor
import com.github.unchama.menuinventory.router.CanOpen
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.menus.BuildMainMenu
import com.github.unchama.seichiassist.menus.stickmenu.{FirstPage, StickMenu}
import org.bukkit.command.TabExecutor

/**
 * 棒メニューを開くコマンド
 * @author
 *   KisaragiEffective
 */
object StickMenuCommand {
  def executorA(
    implicit ioCanOpenStickMenuFirstPage: IO CanOpen FirstPage.type
  ): ContextualExecutor =
    playerCommandBuilder
      .execution { _ => IO.pure(ioCanOpenStickMenuFirstPage.open(StickMenu.firstPage)) }
      .build()

  def executorB(
    implicit ioCanOpenBuildMainMenu: IO CanOpen BuildMainMenu.type
  ): ContextualExecutor =
    playerCommandBuilder
      .execution { _ => IO.pure(ioCanOpenBuildMainMenu.open(BuildMainMenu)) }
      .build()

  def executor(
    implicit ioCanOpenStickMenuFirstPage: IO CanOpen FirstPage.type,
    ioCanOpenBuildMainMenu: IO CanOpen BuildMainMenu.type
  ): TabExecutor =
    BranchedExecutor(
      Map("b" -> executorB),
      whenArgInsufficient = Some(executorA),
      whenBranchNotFound = Some(executorA)
    ).asNonBlockingTabExecutor()
}
