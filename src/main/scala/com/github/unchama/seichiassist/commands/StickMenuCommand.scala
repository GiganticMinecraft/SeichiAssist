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
    playerCommandBuilder.buildWithEffectAsExecution(
      ioCanOpenStickMenuFirstPage.open(StickMenu.firstPage)
    )

  def executorB(
    implicit ioCanOpenBuildMainMenu: IO CanOpen BuildMainMenu.type
  ): ContextualExecutor =
    playerCommandBuilder.buildWithEffectAsExecution(ioCanOpenBuildMainMenu.open(BuildMainMenu))

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
