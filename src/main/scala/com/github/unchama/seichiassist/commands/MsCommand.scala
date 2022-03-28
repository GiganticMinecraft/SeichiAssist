package com.github.unchama.seichiassist.commands

import cats.effect.IO
import com.github.unchama.contextualexecutor.builder.ParserResponse.{failWith, succeedWith}
import com.github.unchama.menuinventory.LayoutPreparationContext
import com.github.unchama.seichiassist.commands.contextual.builder.BuilderTemplates.playerCommandBuilder
import com.github.unchama.seichiassist.concurrent.PluginExecutionContexts.onMainThread
import com.github.unchama.seichiassist.menus.minestack.CategorizedMineStackMenu
import com.github.unchama.seichiassist.minestack.MineStackObjectCategory
import com.github.unchama.targetedeffect.commandsender.MessageEffect
import org.bukkit.command.TabExecutor

object MsCommand {

  def executor(
    implicit categorizedMineStackMenuEnvironment: CategorizedMineStackMenu.Environment,
    layoutPreparationContext: LayoutPreparationContext
  ): TabExecutor = playerCommandBuilder
    .argumentsParsers(
      List(
        category => {
          category.toIntOption match {
            case Some(categoryValue) =>
              MineStackObjectCategory.fromSerializedValue(categoryValue) match {
                case Some(_) => succeedWith(categoryValue)
                case None    => failWith("指定されたカテゴリは存在しません。")
              }
            case None => failWith("カテゴリは数字で入力してください。")
          }
        },
        page => {
          page.toIntOption match {
            case Some(pageNum) =>
              if (pageNum < 0) {
                failWith("ページ数は正の値を指定してください。")
              } else {
                succeedWith(page)
              }
            case None =>
              failWith("ページ数は数字で入力してください。")
          }
        }
      )
    )
    .execution { context => }
    .build()
    .asNonBlockingTabExecutor()

}
